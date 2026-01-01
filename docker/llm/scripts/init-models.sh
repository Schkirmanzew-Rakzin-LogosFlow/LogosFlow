#!/bin/sh
set -eu

MODELS_DIR="${MODELS_DIR:-/root/modelslinks}"

# Defaults for context sizes and other parameters, configurable via environment variables.
DEFAULT_CTX="${DEFAULT_CTX:-4096}"
EXTRA_CTX_LIST="${EXTRA_CTX_LIST:-8192}"
VL_CTX="${VL_CTX:-4096}"
DEFAULT_TEMPERATURE="${DEFAULT_TEMPERATURE:-0.7}"
NUM_THREAD="${NUM_THREAD:-}"

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

# Create a model in Ollama from a GGUF file with a specific context size.
create_model() {
  model_tag="$1"
  gguf_path="$2"
  ctx="$3"

  if ollama show "$model_tag" >/dev/null 2>&1; then
    echo "[model_prep] exists: $model_tag"
    return 0
  fi

  tmpfile="/tmp/Modelfile.$$"
  {
    echo "FROM $gguf_path"
    echo "PARAMETER num_ctx $ctx"
    echo "PARAMETER temperature $DEFAULT_TEMPERATURE"
    if [ -n "$NUM_THREAD" ]; then
      echo "PARAMETER num_thread $NUM_THREAD"
    fi
  } > "$tmpfile"

  echo "[model_prep] creating: $model_tag (ctx=$ctx) from $gguf_path"
  ollama create "$model_tag" -f "$tmpfile"
  rm -f "$tmpfile"
}

# Main loop to process all .gguf files in the models directory.
found=0
for f in "$MODELS_DIR"/*.gguf; do
  if [ ! -f "$f" ]; then
    continue
  fi
  found=1

  base="$(basename "$f" .gguf)"
  name="$(sanitize_name "$base")"

  ctx_main="$(ctx_for_file "$base")"
  create_model "${name}:ctx${ctx_main}" "$f" "$ctx_main"

  if [ -n "$EXTRA_CTX_LIST" ]; then
    for extra in $EXTRA_CTX_LIST; do
      if [ "$extra" = "$ctx_main" ]; then
        continue
      fi
      create_model "${name}:ctx${extra}" "$f" "$extra"
    done
  fi

done

if [ "$found" -eq 0 ]; then
  echo "[model_prep] WARNING: no *.gguf found in $MODELS_DIR"
fi

echo "[model_prep] done."
