include .env
include make-tasks/Makefile.*

# Do NOT include the sub-makefiles; we’ll call them with -C
# (Leave Maven include out as well unless you really need its vars)

.DEFAULT_GOAL := help

.PHONY: help
help:
	@echo "Available targets:"
	@$(MAKE) -C make-tasks -f Makefile.docker help
	@echo "---"
	@if [ -f make-tasks/Makefile.maven ]; then \
	  $(MAKE) -C make-tasks -f Makefile.maven help; \
	else \
	  echo "(make-tasks/Makefile.maven not found — skipping)"; \
	fi

# LLM wrappers (no collisions now)
.PHONY: llm-start llm-stop llm-restart llm-status llm-logs
llm-start:      ; $(MAKE) -C make-tasks -f Makefile.llm llm-start
llm-stop:       ; $(MAKE) -C make-tasks -f Makefile.llm llm-stop
llm-restart:    ; $(MAKE) -C make-tasks -f Makefile.llm llm-restart
llm-status:     ; $(MAKE) -C make-tasks -f Makefile.llm llm-status
llm-logs:       ; $(MAKE) -C make-tasks -f Makefile.llm llm-logs

# Docker/MCPO wrappers
.PHONY: llm-mcpo-build llm-mcpo-up llm-mcpo-down llm-mcpo-logs curl-health curl-scrape curl-mcp-schema curl-mcp-sse clean-all
llm-mcpo-build: ; $(MAKE) -C make-tasks -f Makefile.docker llm-mcpo-build
llm-mcpo-up:    ; $(MAKE) -C make-tasks -f Makefile.docker llm-mcpo-up
llm-mcpo-down:  ; $(MAKE) -C make-tasks -f Makefile.docker llm-mcpo-down
llm-mcpo-logs:  ; $(MAKE) -C make-tasks -f Makefile.docker llm-mcpo-logs

curl-health: ; $(MAKE) -C make-tasks -f Makefile.docker curl-health
curl-mcpo-openapi:    ; $(MAKE) -C make-tasks -f Makefile.docker curl-mcpo-openapi
curl-mcpo-docs:  ; $(MAKE) -C make-tasks -f Makefile.docker curl-mcpo-docs
curl-crawl4ai-openapi:  ; $(MAKE) -C make-tasks -f Makefile.docker curl-crawl4ai-openapi
curl-crawl4ai-docs:  ; $(MAKE) -C make-tasks -f Makefile.docker curl-crawl4ai-docs
curl-scrape:  ; $(MAKE) -C make-tasks -f Makefile.docker curl-scrape

clean-all:  ; $(MAKE) -C make-tasks -f Makefile.docker clean-all


# switch-qwen-coder: ; $(MAKE) -f make-tasks/Makefile.llm switch-qwen-coder
# switch-deepseek-qwen3: ; $(MAKE) -f make-tasks/Makefile.llm switch-deepseek-qwen3
# switch-qwen3-4b: ; $(MAKE) -f make-tasks/Makefile.llm switch-qwen3-4b
# switch-qwen3-8b: ; $(MAKE) -f make-tasks/Makefile.llm switch-qwen3-8b
# switch-mistral-7b: ; $(MAKE) -f make-tasks/Makefile.llm switch-mistral-7b
# unload-models:    ; $(MAKE) -f make-tasks/Makefile.llm unload-models

# llm-test-qwen-coder: ; $(MAKE) -f make-tasks/Makefile.llm test-qwen-coder
# llm-test-deepseek-qwen3: ; $(MAKE) -f make-tasks/Makefile.llm test-deepseek-qwen3
# llm-test-qwen3-4b: ; $(MAKE) -f make-tasks/Makefile.llm test-qwen3-4b
# llm-test-qwen3-8b: ; $(MAKE) -f make-tasks/Makefile.llm test-qwen3-8b
# llm-test-mistral-7b: ; $(MAKE) -f make-tasks/Makefile.llm test-mistral-7b


