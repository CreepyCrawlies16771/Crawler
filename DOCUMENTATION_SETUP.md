# Documentation Setup Complete

This document summarizes the documentation restructuring for the Crawler library.

## What Changed

### 1. Dark Theme Applied

All HTML documentation now uses a dark theme with green accents:

- **Background**: Dark gray (`#111827`)
- **Text**: Light gray (`#E5E7EB`)
- **Accents**: Green (`#4ADE80`, `#22C55E`, `#16A34A`)
- **Callouts**: Color-coded (green for success, orange for warning, red for error, blue for info)

Updated files:
- `docs-html/css/style.css` - Complete dark theme stylesheet
- `docs-html/index.html` - Dark theme landing page
- `docs-html/TEMPLATE.html` - Dark theme template for new pages

### 2. Removed All Emojis

All emoji characters have been removed from:
- `docs-html/index.html`
- `docs-html/TEMPLATE.html`
- `docs/USER_GUIDE.md`
- `docs-html/css/style.css` (references to emojis in CSS comments)
- `DOCUMENTATION_AI_INSTRUCTIONS.md`

### 3. Simplified User Guide

`docs/USER_GUIDE.md` has been completely rewritten to focus on:

- Simplicity over features
- Getting robot running in 30 minutes
- Honest about what Crawler is and isn't
- No false promises of features not included
- Simple, real code examples

### 4. Markdown-First Architecture

Documentation now follows a simple pattern:

1. **Content lives in markdown** (`/docs/` folder)
2. **HTML pages link to markdown** (`/docs-html/` folder)
3. **Easy to add new pages** - Just create markdown file and link it

No need to create separate HTML pages for each topic.

### 5. Updated AI Instructions

`DOCUMENTATION_AI_INSTRUCTIONS.md` now includes:

- Philosophy of simplicity
- Markdown-first approach
- Dark theme color scheme
- Clear guidelines on what NOT to promise
- Simple templates for new guides
- Checklist for finishing documentation

## File Structure

```
Crawler/
├── docs/
│   ├── USER_GUIDE.md                     # Main user guide
│   ├── installation.md                   # Installation tutorial
│   ├── setup.md                          # Hardware setup
│   ├── first-auto.md                     # First autonomous
│   ├── first-teleop.md                   # First teleoperation
│   ├── robot-oriented.md                 # Robot movement basics
│   ├── configuration.md                  # Configuration guide
│   ├── troubleshooting.md                # Troubleshooting
│   ├── ftc-dashboard.md                  # Dashboard guide
│   ├── pure-pursuit.md                   # Path following
│   ├── tuning-guide.md                   # Tuning guide
│   ├── tuning-overview.md                # Tuning overview
│   └── example.md                        # Code examples
│
├── docs-html/
│   ├── index.html                        # Landing page (DARK THEME)
│   ├── TEMPLATE.html                     # Template for new HTML pages
│   ├── README.md                         # Documentation guide
│   ├── css/
│   │   └── style.css                     # Dark theme styles
│   ├── js/
│   │   └── scripts.js                    # Interactive features
│   └── assets/
│       └── images/                       # Images folder (empty - ready for logo)
│
├── DOCUMENTATION_AI_INSTRUCTIONS.md      # Guidelines for AI assistants
└── DOCUMENTATION_SETUP.md                # This file
```

## Adding the Crawler Logo

The logo image is not included (it's a binary file). To add it:

1. Get the Crawler logo PNG file
2. Save it as: `docs-html/assets/images/crawler-logo.png`
3. Update `docs-html/index.html` header to display it (currently shows text only)

See `docs-html/README.md` for instructions on how to add the logo to the HTML.

## How to Add New Documentation

### To Add a New Markdown Guide

1. Create a file in `/docs/your-topic.md`
2. Use this structure:

```markdown
# Your Topic Title

Brief introduction.

## What You'll Learn

- Point 1
- Point 2

## Prerequisites

What's needed.

## Steps

Content here...

## Example Code

```java
// Code example
```

## Troubleshooting

Common issues.

## What's Next

Related topics.
```

3. Link to it from `docs/index.md` (or any page) with a normal markdown link:

```markdown
[Your Topic](your-topic.md)
```

> **Link rules (important):**
> - In **markdown sources** (`docs/*.md`), always link with `.md` targets — they render on GitHub. The build (`generate-docs.js`) rewrites relative `.md` links to `.html` automatically, so the generated site works too.
> - In **hand-written HTML** (e.g. `docs-html/TEMPLATE.html`), always link with `.html` targets — `.md` links 404 on the served site.
> - The site is generated from `docs/*.md`; `docs-html/*.html` is rebuilt on every `npm run build` (and in CI), so don't hand-edit generated pages — add new pages to `PAGE_ORDER` / `NAV_GROUPS` in `generate-docs.js` if they should appear in the sidebar.

### To Add a New Tutorial

1. Create file in `/docs/tutorial-name.md`
2. Link to it from `docs/index.md` (markdown, `.md` target) or from hand-written HTML with an `.html` target:

```markdown
[tutorial-name](tutorial-name.md)
```

## Viewing the Documentation

### One command (recommended)

```bash
./start-docs.sh          # installs deps, builds, serves, opens the browser
# or, if you prefer npm directly:
npm run dev
```

The dev server (backed by `serve-docs.js`):

- builds the site from `docs/` (content) + `site/` (styles, scripts, logo) into `docs-html/`
- serves it at `http://localhost:3000` — set `PORT=8080 npm run dev` to change the port
- **opens your default browser automatically**
- live-reloads the open tab whenever a `docs/*.md` file or a `site/` asset changes
- skip the auto-open with `NO_OPEN=1 npm run dev` (or `npm run dev -- --no-open`)

### Just build (no server)

```bash
npm run build            # writes static HTML + assets to docs-html/
```

## Key Principles

### Crawler Philosophy

Crawler is:
- Simple to understand
- Fast to set up (30 minutes)
- Focused on common tasks
- Intentionally limited in scope

Documentation reflects this.

### Documentation Philosophy

Documentation should:
- Be easy to understand
- Use simple language
- Include working code
- Not promise features Crawler doesn't have
- Help people succeed quickly

### What NOT to Do

- Don't add emojis
- Don't promise advanced features
- Don't write for power users
- Don't create overly complex pages
- Don't explain Java basics
- Don't make pages too long

## Next Steps

1. View the documentation at `docs-html/index.html`
2. Read `docs/USER_GUIDE.md` for the user experience
3. Check `docs-html/README.md` for technical details
4. Review `DOCUMENTATION_AI_INSTRUCTIONS.md` for writing guidelines
5. Add the Crawler logo when available

## Questions?

For guidance on writing new documentation, see:

- `DOCUMENTATION_AI_INSTRUCTIONS.md` - Complete writing guidelines
- `docs-html/README.md` - Technical details
- `docs/USER_GUIDE.md` - Example of simplified documentation

---

All documentation is now ready for expansion with a simple, clean, and user-friendly structure.
