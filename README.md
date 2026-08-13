# Crawler Docs

Static documentation site for **Crawler** — the FTC pathing library for
[Team 16771, Creepy Crawlies](https://github.com/CreepyCrawlies16771/Crawler) —
deployed to GitHub Pages.

> **This branch (`site`) contains only the docs site.** The library source code
> lives on the `main` branch, and a clean, ready-to-build FTC project with Crawler
> installed lives on the `starter` branch.

## Quick start (local)

```bash
./start-docs.sh        # Linux/macOS: install deps, build, serve, open browser
start-docs.bat         # Windows: same
```

or manually:

```bash
npm install
npm run dev            # builds docs/ + site/ -> docs-html/, serves at http://localhost:3000
```

## Build

```bash
npm run build          # markdown (docs/) + static assets (site/) -> static site in docs-html/
```

`docs-html/` is generated and git-ignored — never commit it.


The live site is published at:

```
https://creepycrawlies16771.github.io/Crawler/
```

## Structure

| Path | Purpose |
| --- | --- |
| `docs/*.md` | Markdown content — **edit these** |
| `site/` | Static assets (CSS, JS, logo) copied into every build |
| `generate-docs.js` | Markdown → HTML generator (page order, sidebar, theme) |
| `serve-docs.js` | Local dev server with live reload |
| `package.json` | Build scripts (`npm run build`, `npm run dev`) |
| `docs-html/` | Generated output (git-ignored) |
| `.github/workflows/pages.yml` | GitHub Pages build + deploy |
