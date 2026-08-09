# Crawler Docs

Static documentation site for **Crawler** — the FTC pathing library for
[Team 16771, Creepy Crawlies](https://github.com/CreepyCrawlies16771/Crawler) —
deployed to GitHub Pages.

> **This branch (`site`) contains only the docs site.** The library source code
> lives on the `dev` / `main` branches.

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

## Publish to GitHub Pages

The repository includes a GitHub Actions workflow (`.github/workflows/pages.yml`)
that rebuilds the site and deploys it automatically:

1. **One-time setup:** go to *Settings → Pages* and set **Source** to **GitHub Actions**.
2. Push to the `site` branch — the workflow builds `docs-html/` and deploys it.
3. (Or deploy manually: *Actions → Deploy docs to GitHub Pages → Run workflow*.)

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
