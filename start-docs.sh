#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  Crawler docs — one-command testing
#
#  Builds the site from docs/ + site/, starts the dev server, and opens your
#  browser automatically. Edits to any markdown or site/ asset are rebuilt
#  and the open tab live-reloads.
#
#  Usage:
#    ./start-docs.sh              # build + serve + open browser
#    NO_OPEN=1 ./start-docs.sh    # skip opening the browser
#    PORT=8080 ./start-docs.sh    # serve on a custom port
# ─────────────────────────────────────────────────────────────────────────────
set -e
cd "$(dirname "$0")"

if [ ! -d node_modules ]; then
  echo "📦 Installing dependencies (first run only)..."
  npm install
fi

echo "🚀 Starting Crawler docs dev server..."
npm run dev
