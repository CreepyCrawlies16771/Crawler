@echo off
REM ─────────────────────────────────────────────────────────────────────────────
REM  Crawler docs — one-command testing (Windows)
REM
REM  Builds the site from docs/ + site/, starts the dev server, and opens your
REM  browser automatically. Edits to any markdown or site/ asset are rebuilt
REM  and the open tab live-reloads.
REM
REM  Usage:
REM    start-docs.bat              build + serve + open browser
REM    set NO_OPEN=1 & start-docs.bat   skip opening the browser
REM    set PORT=8080 & start-docs.bat   serve on a custom port
REM ─────────────────────────────────────────────────────────────────────────────
cd /d "%~dp0"

if not exist node_modules (
  echo [1/2] Installing dependencies (first run only)...
  call npm install
)

echo [2/2] Starting Crawler docs dev server...
call npm run dev
