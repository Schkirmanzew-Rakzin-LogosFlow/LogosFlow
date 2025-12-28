# 🌟 OpenAPI Tool Servers

This repository provides reference OpenAPI Tool Server implementations making it easy and secure for developers to integrate external tooling and data sources into LLM agents and workflows. Designed for maximum ease of use and minimal learning curve, these implementations utilize the widely adopted and battle-tested [OpenAPI specification](https://www.openapis.org/) as the standard protocol.

By leveraging OpenAPI, we eliminate the need for a proprietary or unfamiliar communication protocol, ensuring you can quickly and confidently build or integrate servers. This means less time spent figuring out custom interfaces and more time building powerful tools that enhance your AI applications.

## ☝️ Why OpenAPI?

* **Established Standard**: OpenAPI is a widely used, production-proven API standard backed by thousands of tools, companies, and communities.
* **No Reinventing the Wheel**: No additional documentation or proprietary spec confusion. If you build REST APIs or use OpenAPI today, you're already set.
* **Easy Integration & Hosting**: Deploy your tool servers externally or locally without vendor lock-in or complex configurations.
* **Strong Security Focus**: Built around HTTP/REST APIs, OpenAPI inherently supports widely used, secure communication methods including HTTPS and well-proven authentication standards (OAuth, JWT, API Keys).
* **Future-Friendly & Stable**: Unlike less mature or experimental protocols, OpenAPI promises reliability, stability, and long-term community support.

## 🚀 Quickstart

Get started quickly with our reference FastAPI-based implementations provided in the `servers/` directory. (You can adapt these examples into your preferred stack as needed, such as using [FastAPI](https://fastapi.tiangolo.com/), [FastOpenAPI](https://github.com/mr-fatalyst/fastopenapi) or any other OpenAPI-compatible library):

```bash
git clone https://github.com/open-webui/openapi-servers
cd openapi-servers

# Example: Installing dependencies for a specific server 'filesystem'
cd servers/filesystem
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --reload
```

Or using Docker:

```bash
cd servers/filesystem
docker compose up
```

## 📂 Server Reference Implementations

Reference implementations provided in this repository demonstrate common use-cases clearly and simply:

* **Filesystem Access** - Manage local file operations safely with configurable restrictions.
* **Git Server** - Expose Git repositories for searching, reading, and possibly writing via controlled API endpoints.
* **Memory & Knowledge Graph** - Persistent memory management and semantic knowledge querying using popular and reliable storage techniques.
* **Weather Server** - Provide current weather conditions and forecasts from trusted public APIs.
* **Get User Info Server** - Access and return enriched user profile information from authentication providers or internal systems.
* **SQL Chat Server** - Connect to SQL databases and automatically generate, execute, and optimize queries based on your database schema and natural language input.
* **External RAG Tool Server** - Connect and execute your own Retrieval-Augmented Generation (RAG) pipelines as callable API tools.

## 🔌 Bridge MCP → OpenAPI (Optional)

For the easiest way to expose your MCP tools as OpenAPI-compatible APIs, we recommend using [mcpo](https://github.com/open-webui/mcpo). This enables tool providers who initially implemented MCP servers to expose them effortlessly as standard OpenAPI-compatible APIs.

**Quick Usage:**

```bash
uvx mcpo --port 8000 -- uvx mcp-server-time --local-timezone=America/New_York
```

## 🔃 Bridge OpenAPI → MCP (Optional)

Several community-maintained projects are available:

* **🌉 openapi-mcp-server** - Acts as a translator from any OpenAPI spec to an MCP tool
* **🔁 mcp-openapi-server** - A lightweight adapter that converts OpenAPI-described endpoints
* **🌀 mcp-openapi-proxy** - Wraps OpenAPI endpoints in a proxy
* **⚡ fastapi_mcp** - A FastAPI extension for serving native FastAPI endpoints through MCP protocol

## 📜 License

Licensed under MIT License.

## 🌱 Community

* For discussions and announcements, visit the [Community Discussions](https://github.com/open-webui/openapi-servers/discussions) page
* Have ideas or feedback? Please open an issue!

---
Generated from [open-webui/openapi-servers](https://github.com/open-webui/openapi-servers) using crawl4ai and MarkItDown MCP servers.
