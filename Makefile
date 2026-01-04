# Makefile
# This file provides a centralized command interface for managing the project's Docker stacks.

SHELL := /bin/bash
.DEFAULT_GOAL := help

# Combine all compose files for full-stack operations.
COMPOSE_ALL   := -f docker-compose.infra.yml -f docker-compose.llm.yml -f docker-compose.ui.yml

.PHONY: help
help: ## ✨ Show this help message
	@echo "Usage: make [command]"
	@echo ""
	@echo "Full Stack Management:"
	@grep -E '^[a-zA-Z0-9_-]+:.*?##' $(MAKEFILE_LIST) | grep -E '🚀|🛑|🔄|📜|📊' | sed -e 's/:.*##/:/' | column -t -s':'
	@echo ""
	@echo "Individual Stack Commands:"
	@echo "  infra-up, infra-down, infra-logs"
	@echo "  llm-up,   llm-down,   llm-logs"
	@echo "  ui-up,    ui-down,    ui-logs"
	@echo ""
	@echo "Status & Cleanup:"
	@grep -E '^[a-zA-Z0-9_-]+:.*?##' $(MAKEFILE_LIST) | grep -E '📈|🧹' | sed -e 's/:.*##/:/' | column -t -s':'

# --- Full Stack Management ---
.PHONY: up down restart logs ps
up: ## 🚀 Start all services (infra, llm, ui)
	@echo "Starting all services..."
	@docker compose $(COMPOSE_ALL) up -d --build

down: ## 🛑 Stop all services
	@echo "Stopping all services..."
	@docker compose $(COMPOSE_ALL) down

restart: ## 🔄 Restart all services
	$(MAKE) down
	$(MAKE) up

logs: ## 📜 View logs for all services
	@docker compose $(COMPOSE_ALL) logs -f

ps: ## 📊 Show status of all running containers
	@docker compose $(COMPOSE_ALL) ps

# --- Individual Stack Management ---
.PHONY: infra-up infra-down infra-logs llm-up llm-down llm-logs ui-up ui-down ui-logs

infra-up: ; $(MAKE) -C make-tasks -f Makefile.infra infra-up
infra-down: ; $(MAKE) -C make-tasks -f Makefile.infra infra-down
infra-logs: ; $(MAKE) -C make-tasks -f Makefile.infra infra-logs

llm-up: ; $(MAKE) -C make-tasks -f Makefile.llm llm-up
llm-down: ; $(MAKE) -C make-tasks -f Makefile.llm llm-down
llm-logs: ; $(MAKE) -C make-tasks -f Makefile.llm llm-logs
llm-models: ; $(MAKE) -C make-tasks -f Makefile.llm llm-models
llm-stop: ; $(MAKE) -C make-tasks -f Makefile.llm llm-stop
llm-load: ; $(MAKE) -C make-tasks -f Makefile.llm llm-load
llm-unload: ; $(MAKE) -C make-tasks -f Makefile.llm llm-unload
llm-unload-all: ; $(MAKE) -C make-tasks -f Makefile.llm llm-unload-all

ui-up: ; $(MAKE) -C make-tasks -f Makefile.ui ui-up
ui-down: ; $(MAKE) -C make-tasks -f Makefile.ui ui-down
ui-logs: ; $(MAKE) -C make-tasks -f Makefile.ui ui-logs

# --- Java Build Management ---
.PHONY: mvn-rebuild-all
mvn-rebuild-all: ## ☕ Rebuild all Java applications
	@$(MAKE) -C make-tasks -f Makefile.maven mvn/rebuild-all

# --- Status & Cleanup ---
.PHONY: status clean
status: ## 📈 Show status of LLM services and available models
	@echo "==> Running Containers:"
	@docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
	@echo "\n==> LiteLLM Models (http://localhost:4000):"
	@curl -s http://localhost:4000/v1/models | jq -r '.data[].id' || echo "LiteLLM not responding."
	@echo "\n==> Ollama Models (http://localhost:11434):"
	@curl -s http://localhost:11434/api/tags | jq -r '.models[].name' || echo "Ollama not responding."

clean: down ## 🧹 Stop all services and prune Docker system
	@echo "Cleaning up Docker system..."
	docker system prune -af
	@echo "Removing Docker volumes..."
	docker volume rm logosflow_llm_models logosflow_litellm_data logosflow_openwebui_data || true
