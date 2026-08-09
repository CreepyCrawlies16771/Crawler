# Documentation Setup

How the Crawler documentation site is structured, built, and published.

## Architecture (markdown-first)

1. **Content lives in markdown** — `docs/*.md`. This is what you edit.
2. **Static assets live in `site/`** — CSS, JS (Canvas UI effects), and the Crawler logo. These are copied into every build.
3. **`generate-docs.js` builds the site** — it renders each `docs/*.md` file into a styled HTML page (sidebar nav, search palette, syntax highlighting, dark theme) and copies `site/` assets, writing everything to `docs-html/`.
4. **`docs-html/` is generated output** — it is git-ignored and rebuilt on every `npm run build` (and in CI). Never hand-edit it.

## File structure

```
├── docs/                          # Markdown content — EDIT THESE
│   ├── index.md
│   ├── USER_GUIDE.md
│   ├── installation.md
│   ├── setup.md
│   ├── first-auto.md
│   ├── first-teleop.md
│   ├── example.md
│   ├── robot-oriented.md
│   ├── configuration.md
│   ├── troubleshooting.md
│   ├── ftc-dashboard.md
│   ├── pure-pursuit.md
│   ├── tuning-guide.md
│   ├── tuning-overview.md
│   ├── tuning.md
│   ├── errors.md
│   └── api-reference.md
├── site/                          # Static assets (css/, js/, assets/images/)
├── generate-docs.js               # Markdown → HTML generator (page order, sidebar, theme)
├── serve-docs.js                  # Local dev server with live reload
├── package.json                   # `npm run build` / `npm run dev`
└── .github/workflows/pages.yml    # GitHub Pages build + deploy
```

## Adding a new page

1. Create `docs/your-topic.md`.
2. If it should appear in the sidebar, add it to `PAGE_ORDER` / `NAV_GROUPS` in `generate-docs.js`.
3. Link to it from other pages.

> **Link rules (important):**
> - In **markdown sources** (`docs/*.md`), always link with `.md` targets — they render on GitHub. The build (`generate-docs.js`) rewrites relative `.md` links to `.html` automatically, so the generated site works too.
> - The site is generated from `docs/*.md`; `docs-html/*.html` is rebuilt on every `npm run build` (and in CI), so don't hand-edit generated pages.

## Viewing the documentation

### One command (recommended)

```bash
./start-docs.sh          # installs deps, builds, serves, opens the browser
# or, if you prefer npm directly:
npm run dev
```

The dev server (backed by `serve-docs.js`):

- builds the site from `docs/` (content) + `site/` (styles, scripts, logo) into `docs-html/`
- serves it at `http://localhost:3000` — set `PORT=8080 npm run dev` to change the port
- opens your default browser automatically
- live-reloads the open tab whenever a `docs/*.md` file or a `site/` asset changes

### Just build (no server)

```bash
npm run build            # writes static HTML + assets to docs-html/
```

## Publishing to GitHub Pages

`.github/workflows/pages.yml` rebuilds the site and deploys it automatically:

1. **One-time setup:** repo *Settings → Pages* → Source: **GitHub Actions**.
2. Push to the `site` branch (or run *Actions → Deploy docs to GitHub Pages → Run workflow*).
3. Live site: `https://creepycrawlies16771.github.io/Crawler/`

See `README.md` for the full workflow.
