#!/usr/bin/env bash
set -euo pipefail

: "${OLLAMA_HOST:=http://ollama_server:11434}"

# Install CLI + system tools we use
apt-get update -qq
apt-get install -y -qq jq curl >/dev/null 2>&1

echo "==> Pulling models from Ollama registry..."

# Function to pull models with retry
pull_model_with_retry() {
    local model_name="$1"
    local max_attempts=3
    local attempt=1
    
    echo "Pulling model: $model_name"
    
    while [ $attempt -le $max_attempts ]; do
        echo "Attempt $attempt/$max_attempts..."
        
        if curl -fsS -X POST "${OLLAMA_HOST}/api/pull" \
            -H "Content-Type: application/json" \
            -d "{\"name\":\"${model_name}\"}"; then
            echo "Pull successful!"
            return 0
        else
            echo "Pull attempt $attempt failed"
            if [ $attempt -lt $max_attempts ]; then
                echo "Retrying in 10 seconds..."
                sleep 10
            fi
        fi
        
        attempt=$((attempt + 1))
    done
    
    echo "All pull attempts failed for $model_name"
    return 1
}

# Pull the exact models from Ollama registry
echo "==> Pulling Qwen2.5-Coder 7B Q4_K_M..."
pull_model_with_retry "qwen2.5-coder:7b-instruct-q4_K_M" || true

echo "==> Pulling DeepSeek R1 0528 Qwen3 8B Q4_K_M..."
pull_model_with_retry "deepseek/deepseek-r1-0528-qwen3-8b:q4_K_M" || true

echo "==> Pulling Qwen3 4B 2507 Q4_K_M..."
pull_model_with_retry "qwen/qwen3-4b-2507:q4_K_M" || true

echo "==> Pulling Qwen3 8B Q4_K_M..."
pull_model_with_retry "qwen/qwen3-8b:q4_K_M" || true

echo "==> Pulling Mistral 7B Instruct v0.3 Q4_K_M..."
pull_model_with_retry "mistralai/mistral-7b-instruct-v0.3:q4_K_M" || true

# Wait a bit for models to be available
sleep 5

# Create aliases with our expected names
create_alias() {
    local source_model="$1"
    local alias_name="$2"
    echo "==> Creating alias: $alias_name -> $source_model"
    
    # Create a simple Modelfile that references the source model
    local modelfile_content="FROM $source_model"
    
    curl -fsS -X POST "${OLLAMA_HOST}/api/create" \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"${alias_name}\",\"modelfile\":\"${modelfile_content}\"}"
    echo ""
}

# Create our expected model names as aliases
create_alias "qwen2.5-coder:7b-instruct-q4_K_M" "qwen-coder-7b"
create_alias "deepseek/deepseek-r1-0528-qwen3-8b:q4_K_M" "deepseek-qwen3-8b"
create_alias "qwen/qwen3-4b-2507:q4_K_M" "qwen3-4b"
create_alias "qwen/qwen3-8b:q4_K_M" "qwen3-8b"
create_alias "mistralai/mistral-7b-instruct-v0.3:q4_K_M" "mistral-7b"

echo "==> Model init completed."
