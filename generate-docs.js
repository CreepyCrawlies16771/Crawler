#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { marked } = require('marked');

// Watch mode support
const chokidar = require('chokidar');
const isWatchMode = process.argv.includes('--watch');

const DOCS_DIR = path.join(__dirname, 'docs');
const SITE_DIR = path.join(__dirname, 'site');
const OUTPUT_DIR = path.join(__dirname, 'docs-html');

marked.setOptions({
  breaks: true,
  gfm: true,
  pedantic: false
});

// ---------------------------------------------------------------------------
// Site-wide content (edit me!)
// ---------------------------------------------------------------------------

const SITE = {
  repo: 'https://github.com/CreepyCrawlies16771/Crawler',
  releases: 'https://github.com/CreepyCrawlies16771/Crawler/releases',
  branch: 'dev',
  version: 'v1.0.0',
  instagram: 'https://www.instagram.com/creepycrawlies16771',
  org: 'Creepy Crawlies · Team 16771',
  license: 'BSD 3-Clause',
  tagline: 'The friendly FTC pathing library that does the math so your robot can drive itself.'
};

// Large ecosystem cards — add more entries to grow the row.
const ECOSYSTEM_CARDS = [
  {
    href: 'tuning.html',
    icon: 'tuner',
    title: 'Crawler Tuner',
    desc: 'Guided odometry, PID, and path calibration in a single OpMode — with live editing in FTC Dashboard.',
    link: 'Explore the tuner'
  }
];

// Small community / resource cards.
const COMMUNITY_CARDS = [
  { href: SITE.repo, icon: 'github', label: 'GitHub' },
  { href: SITE.instagram, icon: 'instagram', label: 'Instagram' }
];

// Pages that get the API-reference treatment: faint Canvas UI Grid background
// + man-page style definitions.
const API_PAGES = ['configuration', 'robot-oriented', 'pure-pursuit', 'tuning-guide', 'api-reference'];


// Quickstart walkthrough order — pages in this list get a step progress line.
const QUICKSTART_PAGES = ['installation', 'setup', 'first-auto', 'first-teleop', 'example'];

// Top-bar shortcut links.
const TOP_LINKS = [
  { href: 'installation.html', label: 'install' },
  { href: 'first-auto.html', label: 'auto' },
  { href: 'tuning.html', label: 'tuning' },
  { href: 'api-reference.html', label: 'api' },
  { href: 'troubleshooting.html', label: 'help' }
];

// ---------------------------------------------------------------------------
// Icons & logo (inline SVG)
// ---------------------------------------------------------------------------

/** Small crawler mark (hexagon + legs + antennae), stroke = currentColor. */
function logoMark() {
  return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
  <path d="M12 3.4 19.6 7.7v8.6L12 20.6 4.4 16.3V7.7Z"/>
  <path d="M10.6 6.4 9.2 3.6 M13.4 6.4 14.8 3.6"/>
  <path d="M9 10.2 6.4 8.6 M15 10.2 17.6 8.6 M9 13.8 6.4 15.4 M15 13.8 17.6 15.4"/>
  <circle cx="12" cy="12" r="2.1" fill="currentColor" stroke="none"/>
</svg>`;
}

/** Crawler logo lockup for the homepage hero (raster asset shipped in site/assets/images). */
function logoLockup() {
  return `<img src="assets/images/crawler-logo.svg" alt="Crawler logo" width="1024" height="686" decoding="async">`;
}

const ICONS = {
  tuner: () => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
  <path d="M4 7.5h16 M4 16.5h16"/>
  <circle cx="9.5" cy="7.5" r="2.4" fill="var(--bg-1)"/>
  <circle cx="14.5" cy="16.5" r="2.4" fill="var(--bg-1)"/>
</svg>`,
  github: () => `<svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
  <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/>
</svg>`,
  instagram: () => `<svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
  <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/>
</svg>`,
  arrow: () => `<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 8h10 M9 4l4 4-4 4"/></svg>`,
  sun: () => `<svg class="icon-sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
  <circle cx="12" cy="12" r="4.2"/><path d="M12 2.5v2.4M12 19.1v2.4M2.5 12h2.4M19.1 12h2.4M4.9 4.9l1.7 1.7M17.4 17.4l1.7 1.7M19.1 4.9l-1.7 1.7M6.6 17.4l-1.7 1.7"/>
</svg>`,
  moon: () => `<svg class="icon-moon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
  <path d="M20.4 14.2A8.5 8.5 0 0 1 9.8 3.6a8.5 8.5 0 1 0 10.6 10.6Z"/>
</svg>`
};

// ---------------------------------------------------------------------------
// Static assets: copy versioned site/ → docs-html/
// ---------------------------------------------------------------------------

function copyDir(src, dest) {
  if (!fs.existsSync(dest)) fs.mkdirSync(dest, { recursive: true });
  fs.readdirSync(src, { withFileTypes: true }).forEach(entry => {
    const s = path.join(src, entry.name);
    const d = path.join(dest, entry.name);
    if (entry.isDirectory()) copyDir(s, d);
    else fs.copyFileSync(s, d);
  });
}

function copyStaticAssets() {
  if (!fs.existsSync(SITE_DIR)) {
    console.warn('⚠️  site/ folder not found — skipping static assets.');
    return;
  }
  copyDir(SITE_DIR, OUTPUT_DIR);
  console.log('📦 Copied static assets (site/ → docs-html/).');
}

// ---------------------------------------------------------------------------
// Markdown frontmatter
// ---------------------------------------------------------------------------

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function stripFrontmatter(md) {
  const m = md.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n?/);
  if (!m) return { body: md, meta: {} };
  const meta = {};
  m[1].split('\n').forEach(line => {
    const kv = line.match(/^([\w-]+):\s*(.+)$/);
    if (kv) meta[kv[1]] = kv[2].trim();
  });
  return { body: md.slice(m[0].length), meta };
}

// ---------------------------------------------------------------------------
// Navigation model
// ---------------------------------------------------------------------------

const PAGE_ORDER = [
  'index',
  'installation',
  'setup',
  'first-auto',
  'first-teleop',
  'example',
  'tuning',
  'tuning-guide',
  'configuration',
  'robot-oriented',
  'pure-pursuit',
  'api-reference',
  'ftc-dashboard',
  'troubleshooting',
  'errors'
];

const NAV_GROUPS = [
  { label: 'Home', pages: ['index'] },
  { label: 'Getting Started', pages: ['installation', 'setup', 'first-auto', 'first-teleop', 'example'] },
  { label: 'Tuning', pages: ['tuning', 'tuning-guide', 'configuration', 'ftc-dashboard'] },
  { label: 'Movement', pages: ['robot-oriented', 'pure-pursuit', 'api-reference'] },
  { label: 'Help', pages: ['troubleshooting', 'errors'] }
];

function getTableOfContents() {
  const files = fs.readdirSync(DOCS_DIR)
    .filter(f => f.endsWith('.md') && f !== 'readme.md');
  const byName = {};
  files.forEach(file => {
    const name = file.replace('.md', '');
    const title = name === 'index'
      ? 'Welcome'
      : name.split('-').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
    byName[name] = { name, htmlFile: name + '.html', title, file };
  });
  const ordered = [];
  PAGE_ORDER.forEach(n => { if (byName[n]) ordered.push(byName[n]); });
  Object.keys(byName).forEach(n => { if (!PAGE_ORDER.includes(n)) ordered.push(byName[n]); });
  return ordered;
}

function getPageIndex(pageName) {
  return getTableOfContents().findIndex(item => item.name === pageName);
}

function getPrevNext(pageName) {
  const toc = getTableOfContents();
  const i = getPageIndex(pageName);
  return {
    prev: i > 0 ? toc[i - 1] : null,
    next: i >= 0 && i < toc.length - 1 ? toc[i + 1] : null
  };
}

/** Sidebar rendered as a file tree (folder carets + files), not a menu. */
function createNavigation(currentPage = null) {
  let nav = '<nav class="sidebar" id="sidebar" aria-label="Documentation">';
  nav += '<div class="tree">';
  NAV_GROUPS.forEach(group => {
    const pages = group.pages
      .map(name => getTableOfContents().find(t => t.name === name))
      .filter(Boolean);
    const active = pages.some(p => p.name === currentPage);
    nav += `<div class="tree-folder" data-group="${escapeHtml(group.label)}">`;
    nav += `<button type="button" class="tree-caret" aria-expanded="true" aria-label="Toggle folder: ${escapeHtml(group.label)}">▾</button>`;
    nav += `<span class="tree-folder-name">${escapeHtml(group.label)}/</span>`;
    nav += '<ul class="tree-children">';
    pages.forEach(item => {
      const isActive = currentPage === item.name ? ' active' : '';
      nav += `<li><a href="${item.htmlFile}" class="tree-file${isActive}"><span class="tf-name">${escapeHtml(item.file)}</span></a></li>`;
    });
    nav += '</ul></div>';
  });
  nav += '</div></nav>';
  return nav;
}

// ---------------------------------------------------------------------------
// Markdown → HTML transforms
// ---------------------------------------------------------------------------

function languageFromAttrs(attrs) {
  const m = (attrs || '').match(/language-([\w+-]+)/);
  return m ? m[1] : 'code';
}

// ---------------------------------------------------------------------------
// Markdown tab blocks — %%%tabs / %%%tab <Title> / %%%endtabs
//
// Each %%%tab section is marked-parsed on its own, then the whole block is
// replaced with a placeholder token (so marked never touches the inner
// markdown). After the page is parsed the tokens are spliced back in as a
// tab strip + panels; the downstream code-block / callout / link transforms
// then apply inside the panels too.
// ---------------------------------------------------------------------------

let tabRunCounter = 0;
let mdTabGroupCounter = 0;

/** Extract %%%tabs blocks from raw markdown, replacing them with @@@MDTABS:n@@@ tokens. */
function extractTabGroups(mdContent) {
  const groups = [];
  const md = mdContent.replace(
    /%%%tabs\s*\r?\n([\s\S]*?)%%%endtabs/g,
    (whole, inner) => {
      const group = mdTabGroupCounter++;
      const parts = inner.split(/^%%%tab\s+(.+?)\s*$/gm);
      const sections = [];
      for (let i = 1; i + 1 < parts.length; i += 2) {
        const title = (parts[i] || '').trim();
        const content = (parts[i + 1] || '').trim();
        if (title) sections.push({ title, content });
      }
      groups[group] = sections;
      return `@@MDTABS:${group}@@`;
    }
  );
  return { md, groups };
}

function slugifyTitle(title, seen) {
  let slug = title.toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '') || 'tab';
  const base = slug;
  let n = 2;
  while (seen.has(slug)) slug = `${base}-${n++}`;
  seen.add(slug);
  return slug;
}

/** Assemble a stored tab group into a tab strip + panels (panel content is markdown). */
function assembleTabGroup(group, groups) {
  const sections = groups[group] || [];
  if (!sections.length) return '';
  const seen = new Set();
  const entries = sections.map(s => ({ ...s, slug: slugifyTitle(s.title, seen) }));

  const buttons = entries.map((s, i) =>
    `<button type="button" class="md-tab-btn${i === 0 ? ' active' : ''}" role="tab"` +
    ` aria-selected="${i === 0 ? 'true' : 'false'}" aria-controls="md-tab-${s.slug}"` +
    ` data-md-group="${group}" tabindex="${i === 0 ? '0' : '-1'}">${escapeHtml(s.title)}</button>`
  ).join('');

  const panels = entries.map((s, i) =>
    `<div class="md-tab-panel${i === 0 ? '' : ' hidden'}" id="md-tab-${s.slug}"` +
    ` role="tabpanel" data-md-group="${group}" data-pane-index="${i}">${marked.parse(s.content)}</div>`
  ).join('');

  return `<div class="md-tabs" role="tablist" aria-label="Tuner types" data-md-group="${group}">${buttons}</div>${panels}`;
}

function convertMarkdownToHTML(mdContent) {
  const { md, groups } = extractTabGroups(mdContent);
  let htmlContent = marked.parse(md);

  // Splice tab groups back in (strip the <p> wrapper marked adds around the token).
  htmlContent = htmlContent.replace(/<p>@@MDTABS:(\d+)@@<\/p>/g, (m, g) => assembleTabGroup(+g, groups));
  htmlContent = htmlContent.replace(/@@MDTABS:(\d+)@@/g, (m, g) => assembleTabGroup(+g, groups));

  // Rewrite relative cross-page links (foo.md → foo.html). The markdown
  // sources link to each other with .md so they render on GitHub; the built
  // site serves only generated .html pages, so those hrefs must be rewritten.
  // Absolute URLs (GitHub edit links, etc.) are left untouched, and files that
  // merely contain ".md" (e.g. foo.md5, foo.md.png) are not affected.
  htmlContent = htmlContent.replace(/href="([^"]+)"/g, (match, href) =>
    /^(?:[a-z][a-z0-9+.-]*:|\/\/)/i.test(href) || !/\.md(?=[?#]|$)/i.test(href)
      ? match
      : `href="${href.replace(/\.md(?=[?#]|$)/i, '.html')}"`);

  // Code blocks → terminal pane header + copy button
  htmlContent = htmlContent.replace(
    /<pre><code([^>]*)>([\s\S]*?)<\/code><\/pre>/g,
    (match, attrs, code) => {
      const lang = languageFromAttrs(attrs);
      const label = lang === 'code' ? 'code' : lang.toUpperCase();
      const isShell = lang === 'bash' || lang === 'sh' || lang === 'shell';
      const prompt = isShell ? '<span class="code-shell-prompt">$</span>' : '';
      return `<div class="code-block"><div class="code-block-header"><span class="code-lang">${prompt}${label}</span>` +
        `<button class="copy-button" onclick="copyToClipboard(this)" aria-label="Copy code">[copy]</button></div>` +
        `<pre><code${attrs}>${code}</code></pre></div>`;
    }
  );

  // Group consecutive code blocks into tab strips (two adjacent fences in
  // markdown → several variations of the same file on one page).
  htmlContent = htmlContent.replace(
    /<div class="code-block">[\s\S]*?<\/div>(?:\s*<div class="code-block">[\s\S]*?<\/div>)+/g,
    (run) => {
      const runId = ++tabRunCounter;
      const blocks = run.match(/<div class="code-block">[\s\S]*?<\/div>/g) || [];
      const tabs = blocks.map((b, i) => {
        const m = b.match(/<span class="code-lang">([\s\S]*?)<\/span>/);
        const label = (m ? m[1] : 'code')
          .replace(/<span class="code-shell-prompt">\$<\/span>/, '')
          .replace(/<[^>]+>/g, '').trim();
        return `<button type="button" class="code-tab${i === 0 ? ' active' : ''}" role="tab" aria-selected="${i === 0}" aria-controls="code-tab-${runId}-${i}" data-tab-index="${i}">${label}</button>`;
      }).join('');
      const panes = blocks.map((b, i) =>
        `<div class="code-pane${i === 0 ? '' : ' hidden'}" id="code-tab-${runId}-${i}" role="tabpanel" data-pane-index="${i}">${b}</div>`
      ).join('');
      return `<div class="code-tabs" role="tablist" aria-label="Code samples">${tabs}</div>${panes}`;
    }
  );

  // Blockquote callouts (emoji-led) → comment-style admonitions
  htmlContent = htmlContent.replace(
    /<blockquote>\s*<p>([\s\S]*?)<\/p>\s*<\/blockquote>/g,
    (match, inner) => {
      let cls = 'callout-info';
      if (/^(💡|📝|ℹ️?|📌)/.test(inner)) cls = 'callout-tip';
      else if (/^(⚠️?|🔧)/.test(inner)) cls = 'callout-warning';
      else if (/^(🚨|❌|⛔)/.test(inner)) cls = 'callout-danger';
      else if (/^(✅|✔️|🎉)/.test(inner)) cls = 'callout-success';
      return `<div class="callout ${cls}"><p>${inner}</p></div>`;
    }
  );

  return htmlContent;
}

// ---------------------------------------------------------------------------
// Page shell
// ---------------------------------------------------------------------------

const FAVICON = "data:image/svg+xml," + encodeURIComponent(
  "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'>" +
  "<path d='M12 3.4 19.6 7.7v8.6L12 20.6 4.4 16.3V7.7Z' fill='none' stroke='%234ade80' stroke-width='2.4' stroke-linejoin='round'/></svg>"
);

const THEME_INIT_SCRIPT = `<script>
(function(){document.documentElement.classList.add('js');try{var t=localStorage.getItem('crawler-theme');var d=t?t==='dark':window.matchMedia('(prefers-color-scheme: dark)').matches;document.documentElement.setAttribute('data-theme',d?'dark':'light');var m=document.querySelector('meta[name="theme-color"]');if(m){m.setAttribute('content',d?'#0a0a0a':'#f2f2ef');}}catch(e){document.documentElement.setAttribute('data-theme','dark');}})();
</script>`;

function homeSections() {
  const ecosystem = ECOSYSTEM_CARDS.map(card => `
    <a class="ecocard" href="${card.href}">
      <span class="ecocard-icon">${ICONS[card.icon]()}</span>
      <span class="ecocard-title">${card.title}</span>
      <span class="ecocard-desc">${card.desc}</span>
      <span class="ecocard-link">${card.link} ${ICONS.arrow()}</span>
    </a>`).join('');

  const community = COMMUNITY_CARDS.map(card => `
    <a class="soc-card" href="${card.href}" target="_blank" rel="noopener">
      ${ICONS[card.icon]()}<span>${card.label}</span>
    </a>`).join('');

  return `
    <section class="home-section ecosystem reveal">
      <h2 class="section-title">Ecosystem</h2>
      <p class="section-sub">One library — everything you need to go from zero to autonomous.</p>
      <div class="ecosystem-grid">
        ${ecosystem}
      </div>
    </section>
    <section class="home-section community reveal">
      <h2 class="section-title">Community &amp; Resources</h2>
      <div class="community-grid">
        ${community}
      </div>
    </section>`;
}

function bootSequence() {
  return `
      <div class="boot" id="bootSeq" aria-hidden="true">
        <pre class="boot-line" data-line="0">Loading Crawler ${SITE.version} ...</pre>
        <pre class="boot-line" data-line="1">Setup time: <span class="boot-ok">&lt;1hr</span> · Tuning: <span class="boot-ok">guided</span></pre>
        <pre class="boot-line" data-line="2">Localizers: any · Paths: pure pursuit</pre>
        <pre class="boot-line" data-line="3"><span class="boot-ok">ready.</span></pre>
        <pre class="boot-cmd"><span class="pt-prompt">$</span> crawler init <span class="boot-cursor" aria-hidden="true">▊</span></pre>
      </div>`;
}

function stepProgress(pageName) {
  const i = QUICKSTART_PAGES.indexOf(pageName);
  if (i < 0) return '';
  const step = i + 1;
  const total = QUICKSTART_PAGES.length;
  const pct = Math.round((step / total) * 100);
  return `
    <div class="step-progress" data-step="${step}" data-steps="${total}">
      <span class="sp-label">Step ${step} of ${total} — ~${8 + step * 2} min</span>
      <span class="sp-track" aria-hidden="true"><i class="sp-fill" style="width:${pct}%"></i></span>
    </div>`;
}

function generateHTMLPage(title, content, mdFileName, meta = {}) {
  const pageName = mdFileName.replace('.md', '');
  const isIndex = pageName === 'index';
  const safeTitle = escapeHtml(title);
  const description = meta.description || SITE.tagline;
  const safeDescription = escapeHtml(description);
  const navigation = createNavigation(pageName);
  const { prev, next } = getPrevNext(pageName);
  const isApi = API_PAGES.includes(pageName);
  const editUrl = `${SITE.repo}/edit/${SITE.branch}/docs/${mdFileName}`;

  const topLinks = TOP_LINKS.map(l =>
    `<a href="${l.href}"${pageName === l.href.replace('.html', '') ? ' class="active"' : ''}>${l.label}</a>`
  ).join('');

  const hero = isIndex
    ? `<header class="hero hero-home" id="heroHome">
        ${bootSequence()}
        <div class="pr-stage" id="prStage">
          <canvas id="prSource" layoutsubtree aria-hidden="true"></canvas>
          <div class="pr-content" id="prContent">${logoLockup()}</div>
          <canvas id="prOutput" class="pr-canvas" aria-hidden="true"></canvas>
        </div>
        <p class="hero-tagline">${SITE.tagline}</p>
        <div class="cmd-links">
          <a class="cmd-link" href="installation.html">
            <span class="cmd-name">quickstart</span>
            <span class="cmd-desc">installation → first auto</span>
            <span class="cmd-arrow" aria-hidden="true">→</span>
          </a>
          <a class="cmd-link" href="robot-oriented.html">
            <span class="cmd-name">concepts</span>
            <span class="cmd-desc">robot-oriented · pure pursuit</span>
            <span class="cmd-arrow" aria-hidden="true">→</span>
          </a>
          <a class="cmd-link" href="configuration.html">
            <span class="cmd-name">api reference</span>
            <span class="cmd-desc">configuration · tuning guide</span>
            <span class="cmd-arrow" aria-hidden="true">→</span>
          </a>
        </div>
        <div class="hero-actions">
          <a class="btn btn-primary" href="installation.html">Get Started</a>
          <a class="btn btn-ghost" href="tuning.html">Try the Tuner</a>
        </div>
      </header>
      ${homeSections()}`
    : `<header class="hero hero-page">
        <p class="hero-eyebrow">docs/<span class="path-file">${escapeHtml(mdFileName)}</span></p>
        <h1>${safeTitle}</h1>
      </header>`;

  const breadcrumb = isIndex ? '' : `
    <div class="breadcrumb">
      <a href="index.html">~/crawler</a><span class="sep">/</span><span>${safeTitle}</span>
    </div>`;

  const progress = isIndex ? '' : stepProgress(pageName);

  let navButtons = '<div class="page-navigation">';
  if (prev) navButtons += `<a class="nav-button prev-button" href="${prev.htmlFile}"><span class="nav-dir">← Previous</span><span class="nav-title">${prev.title}</span></a>`;
  if (next) navButtons += `<a class="nav-button next-button" href="${next.htmlFile}"><span class="nav-dir">Next →</span><span class="nav-title">${next.title}</span></a>`;
  navButtons += '</div>';

  return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${safeTitle} · Crawler Docs</title>
    <meta name="description" content="${safeDescription}">
    <meta name="theme-color" content="#0a0a0a">
    <link rel="icon" href="${FAVICON}">
    ${THEME_INIT_SCRIPT}
    <link rel="stylesheet" href="css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:ital,wght@0,400;0,500;0,600;0,700;0,800;1,400&display=swap" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/java.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/json.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/gradle.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/xml.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/bash.min.js"></script>
    <script src="js/scripts.js"></script>
</head>
<body class="term-page${isApi ? ' api-page' : ''}" data-page="${pageName}" data-path="docs/${mdFileName}">
  <div class="term-window">
    <div class="term-titlebar">
      <span class="traffic" aria-hidden="true"><i class="t-red"></i><i class="t-yellow"></i><i class="t-green"></i></span>
      <span class="term-path" title="current path">~/crawler/docs/<span class="path-file">${escapeHtml(mdFileName)}</span></span>
      <button type="button" class="nav-toggle" id="navToggle" aria-label="Toggle navigation" aria-expanded="false" aria-controls="sidebar">☰</button>
      <nav class="term-links" aria-label="Quick links">
        ${topLinks}
      </nav>
      <button type="button" class="theme-toggle" id="themeToggle" aria-label="Toggle color theme" title="Toggle dark / light theme">
        ${ICONS.sun()}${ICONS.moon()}
      </button>
      <button type="button" class="palette-trigger" id="paletteTrigger" aria-haspopup="dialog" aria-controls="palettePanel" aria-label="Search documentation (⌘K)">
        <span class="pt-prompt">$</span><span class="pt-label">search docs…</span><kbd class="pt-kbd">⌘K</kbd>
      </button>
    </div>

    <div class="term-body">
      <div class="layout">
        ${navigation}
        <main class="main" id="main">
          ${hero}
          <div class="content-wrap">
            ${breadcrumb}
            ${progress}
            <article class="content">
              ${content}
              ${navButtons}
            </article>
          </div>
        </main>
      </div>
    </div>

    <footer class="statusbar">
      <span class="sb-left">docs/<span class="path-file">${escapeHtml(mdFileName)}</span></span>
      <a class="sb-edit" href="${editUrl}" target="_blank" rel="noopener">[ edit this page on github ]</a>
      <span class="sb-right"><span class="sb-branch">⎇ ${SITE.branch}</span><span class="sb-version">${SITE.version}</span></span>
    </footer>
  </div>

  <!-- Command palette (Canvas UI Glass pane) -->
  <div class="palette-backdrop" id="paletteBackdrop" hidden>
    <div class="palette" id="palettePanel" role="dialog" aria-modal="true" aria-label="Search documentation">
      <div class="glass-host" id="glassHost">
        <canvas id="glassOutput" class="glass-canvas" aria-hidden="true"></canvas>
        <div class="palette-inner">
          <div class="palette-input-row">
            <span class="pt-prompt">$</span>
            <input id="paletteInput" class="palette-input" type="text" placeholder="search docs…"
                   autocomplete="off" spellcheck="false" autocapitalize="off"
                   role="combobox" aria-expanded="true" aria-controls="paletteResults" aria-autocomplete="list" aria-label="Search query">
          </div>
          <ul class="palette-results" id="paletteResults" role="listbox" aria-label="Results"></ul>
          <div class="palette-hints"><span>↑↓ navigate</span><span>↵ open</span><span>esc close</span></div>
        </div>
      </div>
    </div>
  </div>

  <script type="module" src="js/canvasui-bundle.js"></script>
</body>
</html>`;
}

// ---------------------------------------------------------------------------
// Build
// ---------------------------------------------------------------------------

function generateAllDocs() {
  console.log('🔄 Generating documentation...');
  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
  }

  copyStaticAssets();

  const mdFiles = fs.readdirSync(DOCS_DIR).filter(f => f.endsWith('.md'));
  let success = 0;
  mdFiles.forEach(file => {
    const mdPath = path.join(DOCS_DIR, file);
    const htmlPath = path.join(OUTPUT_DIR, file.replace('.md', '.html'));
    try {
      const mdContent = fs.readFileSync(mdPath, 'utf-8');
      const { body, meta } = stripFrontmatter(mdContent);
      const content = convertMarkdownToHTML(body);
      const titleMatch = body.match(/^#\s+(.+)$/m);
      const title = meta.title || (titleMatch ? titleMatch[1] : file.replace('.md', '').replace(/-/g, ' '));
      fs.writeFileSync(htmlPath, generateHTMLPage(title, content, file, meta), 'utf-8');
      console.log(`✅ ${file} → ${file.replace('.md', '.html')}`);
      success++;
    } catch (e) {
      console.error(`❌ ${file}: ${e.stack || e.message}`);
    }
  });
  console.log(`\n✨ Done (${success}/${mdFiles.length}) → ${OUTPUT_DIR}`);
}

if (require.main === module) {
  generateAllDocs();

  if (isWatchMode) {
    console.log('👀 Watching docs/ for changes...');
    chokidar.watch(DOCS_DIR, { ignored: /node_modules/ })
      .on('change', p => { if (p.endsWith('.md')) generateAllDocs(); })
      .on('add', p => { if (p.endsWith('.md')) generateAllDocs(); });
  }
}

module.exports = { generateAllDocs };
