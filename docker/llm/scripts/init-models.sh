#!/bin/sh
set -eu

MODELS_DIR="${MODELS_DIR:-/root/modelslinks}"
MODELS_LIST_FILE="${MODELS_LIST_FILE:-/root/models.list}"

# Defaults for context sizes and other parameters, configurable via environment variables.
DEFAULT_CTX="${DEFAULT_CTX:-4096}"
EXTRA_CTX_LIST="${EXTRA_CTX_LIST:-8192}"
VL_CTX="${VL_CTX:-4096}"
DEFAULT_TEMPERATURE="${DEFAULT_TEMPERATURE:-0.3}"
NUM_THREAD="${NUM_THREAD:-}"
IGNORE_PATTERNS="${IGNORE_PATTERNS:-gemma-3n-e4b}"
PRUNE_ORPHANS="${PRUNE_ORPHANS:-1}"

desired_models=""

# Wait until the Ollama server is responsive.
echo "[model_prep] waiting for Ollama at ${OLLAMA_HOST:-<not set>} ..."
i=0
until ollama list >/dev/null 2>&1; do
  i=$((i+1))
  if [ "$i" -gt 60 ]; then
    echo "[model_prep] ERROR: Ollama not reachable after 60 attempts"
    exit 1
  fi
  sleep 2
done
echo "[model_prep] Ollama is reachable."

# Sanitize the model name to be compliant with Ollama's naming conventions.
sanitize_name() {
  echo "$1" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9._-]+/-/g; s/^-+//; s/-+$//'
}

# Locate a matching projector (mmproj) file for a given model base name.
find_projector() {
  base_lower="$1"
  for proj in "$MODELS_DIR"/mmproj-*.gguf; do
    [ -f "$proj" ] || continue
    proj_base="$(basename "$proj" .gguf)"
    proj_key="$(echo "$proj_base" | tr '[:upper:]' '[:lower:]')"
    proj_key="${proj_key#mmproj-}"
    proj_key="$(echo "$proj_key" | sed -E 's/-(f16|f32|fp16|fp32)$//')"
    if printf '%s' "$base_lower" | grep -Fq "$proj_key"; then
      echo "$proj"
      return 0
    fi
  done
  return 1
}

should_skip() {
  base_lower="$1"
  for pat in $IGNORE_PATTERNS; do
    if printf '%s' "$base_lower" | grep -Fqi "$pat"; then
      return 0
    fi
  done
  return 1
}

# Determine the appropriate context size for a given model file.
ctx_for_file() {
  f="$1"
  low="$(echo "$f" | tr '[:upper:]' '[:lower:]')"
  if echo "$low" | grep -Eq '(^|[^a-z])vl([^a-z]|$)|vision'; then
    echo "$VL_CTX"
  else
    echo "$DEFAULT_CTX"
  fi
}

record_desired() {
  desired_models="$desired_models $1"
}

register_model_tags() {
  name="$1"
  ctx_main="$2"

  record_desired "${name}:ctx${ctx_main}"

  if [ -n "$EXTRA_CTX_LIST" ]; then
    for extra in $EXTRA_CTX_LIST; do
      if [ "$extra" = "$ctx_main" ]; then
        continue
      fi
      record_desired "${name}:ctx${extra}"
    done
  fi
}

# Create a model in Ollama from a GGUF file with a specific context size.
create_model() {
  model_tag="$1"
  gguf_path="$2"
  ctx="$3"
  projector_path="${4:-}"

  record_desired "$model_tag"

  if ollama show "$model_tag" >/dev/null 2>&1; then
    echo "[model_prep] exists: $model_tag"
    return 0
  fi

  write_modelfile() {
    tmpfile="$1"
    projector_line="$2"
    {
      echo "FROM $gguf_path"
      echo "PARAMETER num_ctx $ctx"
      echo "PARAMETER temperature $DEFAULT_TEMPERATURE"
      if [ -n "$NUM_THREAD" ]; then
        echo "PARAMETER num_thread $NUM_THREAD"
      fi
      if [ -n "$projector_line" ]; then
        echo "PROJECTOR $projector_line"
      fi
    } > "$tmpfile"
  }

  attempt_create() {
    modelfile="$1"
    suffix="$2"
    echo "[model_prep] creating: $model_tag${suffix} (ctx=$ctx) from $gguf_path"
    ollama create "$model_tag" -f "$modelfile"
  }

  tmpfile="/tmp/Modelfile.$$"
  write_modelfile "$tmpfile" "$projector_path"

  if ! attempt_create "$tmpfile" ""; then
    if [ -n "$projector_path" ]; then
      echo "[model_prep] WARNING: projector not accepted for $model_tag; retrying without projector"
      write_modelfile "$tmpfile" ""
      attempt_create "$tmpfile" " (no projector)"
    else
      return 1
    fi
  fi

  rm -f "$tmpfile"
}

process_model_entry() {
  base="$1"
  f="$2"

  # Skip projector-only artifacts; they are linked when building the paired vision models.
  if echo "$base" | grep -qi '^mmproj-'; then
    return
  fi

  base_lower="$(echo "$base" | tr '[:upper:]' '[:lower:]')"

  if should_skip "$base_lower"; then
    echo "[model_prep] skipping ignored model: $base"
    return
  fi

  name="$(sanitize_name "$base")"
  ctx_main="$(ctx_for_file "$base")"

  register_model_tags "$name" "$ctx_main"

  if [ ! -f "$f" ]; then
    echo "[model_prep] missing GGUF for listed model: $f"
    return
  fi

  found=1

  projector="$(find_projector "$base_lower" || true)"

  create_model "${name}:ctx${ctx_main}" "$f" "$ctx_main" "$projector"

  if [ -n "$EXTRA_CTX_LIST" ]; then
    for extra in $EXTRA_CTX_LIST; do
      if [ "$extra" = "$ctx_main" ]; then
        continue
      fi
      create_model "${name}:ctx${extra}" "$f" "$extra" "$projector"
    done
  fi
}

# Main loop driven by the explicit models list when present; otherwise fall back to directory scan.
found=0
model_files=""
if [ -f "$MODELS_LIST_FILE" ]; then
  echo "[model_prep] using model list at $MODELS_LIST_FILE"
  model_files="$(sed -e 's/#.*//' -e '/^[[:space:]]*$/d' "$MODELS_LIST_FILE")"
fi

if [ -n "$model_files" ]; then
  for fname in $model_files; do
    process_model_entry "$(basename "$fname" .gguf)" "$MODELS_DIR/$fname"
  done
else
  echo "[model_prep] models list missing or empty; scanning $MODELS_DIR for *.gguf"
  for f in "$MODELS_DIR"/*.gguf; do
    [ -f "$f" ] || continue
    process_model_entry "$(basename "$f" .gguf)" "$f"
  done
fi

prune_orphaned_models() {
  [ "$PRUNE_ORPHANS" = "1" ] || return 0

  existing_models="$(ollama list | tail -n +2 | awk '{print $1}')"
  [ -n "$existing_models" ] || return 0

  desired_sorted="$(printf '%s\n' "$desired_models" | tr ' ' '\n' | sed '/^$/d' | sort -u)"

  for m in $existing_models; do
    if printf '%s\n' "$desired_sorted" | grep -Fxq "$m"; then
      continue
    fi
    # Only prune models that follow the managed :ctx<number> naming convention.
    if ! echo "$m" | grep -Eq ':ctx[0-9]+$'; then
      continue
    fi
    echo "[model_prep] pruning orphaned model: $m"
    ollama rm "$m" || true
  done
}

prune_orphaned_models

if [ "$found" -eq 0 ]; then
  echo "[model_prep] WARNING: no *.gguf found in $MODELS_DIR"
fi

echo "[model_prep] done."
