import hashlib
import json
import os
from datetime import datetime, timezone
from typing import Optional

import httpx
from bs4 import BeautifulSoup
from fastapi import FastAPI, HTTPException, Query, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, HttpUrl, Field
from trafilatura import extract as trafi_extract

# Optional JS rendering with Playwright (loaded on demand)
from playwright.sync_api import sync_playwright

APP_TITLE = "Web Scraper MCP"
APP_VERSION = "1.0.0"
DEFAULT_UA = (
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
)
MAX_HTML_CHARS = int(os.getenv("SCRAPER_MAX_HTML_CHARS", "500000"))

app = FastAPI(title=APP_TITLE, version=APP_VERSION)

# CORS (Traefik already handles it; this is extra safety for direct calls)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], allow_credentials=False,
    allow_methods=["*"], allow_headers=["*"],
)

class ScrapeRequest(BaseModel):
    url: HttpUrl = Field(..., description="Target URL to scrape")
    render_js: bool = Field(False, description="Use headless Chromium if true")
    include_html: bool = Field(False, description="Include raw HTML in response (capped)")
    timeout_ms: int = Field(15000, gt=0, le=60000, description="Overall timeout in ms")
    wait_until: str = Field("networkidle", description="Playwright waitUntil state")
    wait_ms: Optional[int] = Field(None, ge=0, le=60000, description="Extra wait after load (ms)")

class ScrapeResult(BaseModel):
    ok: bool
    fetched_at: str
    input: dict
    result: dict

@app.get("/health")
def health():
    return {"status": "ok", "service": APP_TITLE, "version": APP_VERSION}

@app.get("/api/v1/scrape", response_model=ScrapeResult)
def scrape_get(
    url: HttpUrl = Query(...),
    render_js: bool = Query(False),
    include_html: bool = Query(False),
    timeout_ms: int = Query(15000, gt=0, le=60000),
    wait_until: str = Query("networkidle"),
    wait_ms: Optional[int] = Query(None, ge=0, le=60000),
):
    req = ScrapeRequest(
        url=url, render_js=render_js, include_html=include_html,
        timeout_ms=timeout_ms, wait_until=wait_until, wait_ms=wait_ms
    )
    return _scrape(req)

@app.post("/api/v1/scrape", response_model=ScrapeResult)
async def scrape_post(payload: ScrapeRequest, request: Request):
    return _scrape(payload)

def _scrape(cfg: ScrapeRequest) -> ScrapeResult:
    started = datetime.now(timezone.utc)
    try:
        if cfg.render_js:
            raw_html, final_url, status = _fetch_with_playwright(
                str(cfg.url), cfg.timeout_ms, cfg.wait_until, cfg.wait_ms
            )
        else:
            raw_html, final_url, status = _fetch_with_httpx(str(cfg.url), cfg.timeout_ms)

        if not raw_html:
            raise HTTPException(status_code=502, detail="Empty response body")

        soup = BeautifulSoup(raw_html, "lxml")
        title = (soup.title.string or "").strip() if soup.title else ""

        extracted = trafi_extract(
            raw_html, url=str(cfg.url), include_comments=False, include_tables=False
        ) or ""

        preview = (extracted or soup.get_text("\n", strip=True) or "").strip()
        preview_chars = len(preview)
        raw_html_chars = len(raw_html)

        result = {
            "final_url": final_url,
            "status_code": status,
            "title": title,
            "extracted_text_preview": preview[:4000],
            "extracted_text_chars": preview_chars,
            "raw_html_chars": raw_html_chars,
            "content_hash": hashlib.sha256(raw_html.encode("utf-8", "ignore")).hexdigest(),
        }

        if cfg.include_html:
            if raw_html_chars > MAX_HTML_CHARS:
                result["raw_html_truncated"] = True
                result["raw_html"] = raw_html[:MAX_HTML_CHARS]
            else:
                result["raw_html_truncated"] = False
                result["raw_html"] = raw_html

        return ScrapeResult(
            ok=True,
            fetched_at=started.isoformat(),
            input=json.loads(cfg.model_dump_json()),
            result=result,
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"scrape_error: {e}") from e

def _fetch_with_httpx(url: str, timeout_ms: int):
    headers = {
        "User-Agent": DEFAULT_UA,
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "en,de;q=0.9",
        "Cache-Control": "no-cache",
        "Pragma": "no-cache",
    }
    timeout = httpx.Timeout(timeout_ms / 1000.0)
    with httpx.Client(follow_redirects=True, headers=headers, http2=True, timeout=timeout) as client:
        resp = client.get(url)
        return resp.text, str(resp.url), resp.status_code

def _fetch_with_playwright(url: str, timeout_ms: int, wait_until: str, wait_ms: Optional[int]):
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True, args=["--disable-dev-shm-usage"])
        context = browser.new_context(user_agent=DEFAULT_UA, viewport={"width":1366, "height":768})
        page = context.new_page()
        page.set_default_timeout(timeout_ms)
        page.goto(url, wait_until=wait_until)
        if wait_ms:
            page.wait_for_timeout(wait_ms)
        content = page.content()
        final_url = page.url
        status = 200
        page.close()
        context.close()
        browser.close()
        return content, final_url, status
