# services/web-scraper/Dockerfile
FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

# System deps (curl for healthcheck; Playwright deps for optional JS rendering)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl ca-certificates fonts-liberation libnss3 libx11-6 libx11-xcb1 libxcomposite1 libxcursor1 \
    libxdamage1 libxi6 libxtst6 libxrandr2 libasound2 libatk1.0-0 libatk-bridge2.0-0 libgtk-3-0 \
    libdrm2 libgbm1 libxkbcommon0 libpango-1.0-0 libpangocairo-1.0-0 libatspi2.0-0 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt /app/requirements.txt
RUN pip install -r requirements.txt

# Install Chromium for Playwright (used only if render_js=true)
RUN python -m playwright install --with-deps chromium

COPY app.py /app/app.py

EXPOSE 8000

ENV UVICORN_WORKERS=2
CMD exec gunicorn -w ${UVICORN_WORKERS} -k uvicorn.workers.UvicornWorker app:app --bind 0.0.0.0:8000 --timeout 120 --access-logfile -
