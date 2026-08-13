#!/usr/bin/env node

/**
 * Crawler docs dev server.
 *
 *   npm run dev            # build + serve + open the browser (recommended)
 *   PORT=8080 npm run dev  # custom port
 *   NO_OPEN=1 npm run dev  # don't auto-open the browser
 *   npm run dev -- --no-open  # same
 *
 * Features (zero extra dependencies):
 *  - builds the site from docs/ + site/ into docs-html/
 *  - serves docs-html/ and opens your default browser automatically
 *  - rebuilds + live-reloads every open tab when a markdown file or a
 *    site/ asset changes (SSE + injected script)
 *
 * The same folder (docs-html/) is what gets deployed to GitHub Pages — see
 * .github/workflows/pages.yml.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const ROOT = __dirname;
const DOCS_DIR = path.join(ROOT, 'docs');
const SITE_DIR = path.join(ROOT, 'site');   // versioned static assets (css/js/logo)
const OUTPUT_DIR = path.join(ROOT, 'docs-html');
const PORT = Number(process.env.PORT) || 3000;

const { generateAllDocs } = require('./generate-docs.js');

// ---------------------------------------------------------------------------
// Static file serving
// ---------------------------------------------------------------------------

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.ico': 'image/x-icon',
  '.webp': 'image/webp',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.md': 'text/markdown; charset=utf-8',
  '.txt': 'text/plain; charset=utf-8'
};

/** Resolve a request URL to a file inside OUTPUT_DIR, or null if unsafe/missing. */
function resolveFile(urlPath) {
  let decoded;
  try {
    decoded = decodeURIComponent(urlPath.split('?')[0]);
  } catch (e) {
    return null;
  }
  // Strip query/anchors, prevent path traversal.
  const rel = path.normalize(decoded).replace(/^([/\\])+/, '');
  let file = path.join(OUTPUT_DIR, rel);
  if (!file.startsWith(OUTPUT_DIR)) return null;

  try {
    const stat = fs.statSync(file);
    if (stat.isDirectory()) {
      file = path.join(file, 'index.html');
      if (!fs.existsSync(file)) return null;
    } else if (!fs.existsSync(file)) {
      return null;
    }
  } catch (e) {
    return null;
  }
  return file;
}

function serveFile(req, res, file) {
  const ext = path.extname(file).toLowerCase();
  const body = fs.readFileSync(file);
  let headers = {
    'Content-Type': MIME[ext] || 'application/octet-stream',
    'Cache-Control': 'no-cache'
  };
  // Inject the live-reload script into HTML pages (dev only).
  if (ext === '.html') {
    headers['Content-Length'] = Buffer.byteLength(body) + RELOAD_SCRIPT.length;
    res.writeHead(200, headers);
    res.write(body);
    res.write(RELOAD_SCRIPT);
    res.end();
    return;
  }
  res.writeHead(200, headers);
  res.end(body);
}

// ---------------------------------------------------------------------------
// Auto-open browser
// ---------------------------------------------------------------------------

const shouldOpen = !process.argv.includes('--no-open') && process.env.NO_OPEN !== '1';

function openBrowser(url) {
  const { spawn } = require('child_process');
  let cmd;
  if (process.platform === 'darwin') cmd = ['open', url];
  else if (process.platform === 'win32') cmd = ['cmd', '/c', 'start', '', url];
  else cmd = ['xdg-open', url];

  const child = spawn(cmd[0], cmd.slice(1), { stdio: 'ignore', detached: true });
  child.on('error', () => {
    console.log(`   Couldn't open a browser automatically — visit ${url} manually.`);
  });
  child.unref();
}

// ---------------------------------------------------------------------------
// Live reload (SSE)
// ---------------------------------------------------------------------------

const clients = new Set();

const RELOAD_SCRIPT =
  '<script>' +
  'if(!window.__crawlerReload){window.__crawlerReload=true;' +
  'new EventSource("/__reload").onmessage=function(e){if(e.data==="reload")location.reload();};}' +
  '</script>';

function broadcastReload() {
  for (const res of clients) res.write('data: reload\n\n');
  console.log('🔄 Reloading browsers...');
}

function handleSSE(req, res) {
  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
    Connection: 'keep-alive'
  });
  res.write('data: connected\n\n');
  clients.add(res);
  req.on('close', () => clients.delete(res));
}

// ---------------------------------------------------------------------------
// Rebuild on markdown changes
// ---------------------------------------------------------------------------

// generateAllDocs() is fully synchronous, so a change just triggers one rebuild.
function rebuildAndReload() {
  generateAllDocs();
  broadcastReload();
}

// ---------------------------------------------------------------------------
// Server
// ---------------------------------------------------------------------------

const server = http.createServer((req, res) => {
  const url = req.url || '/';

  if (url.startsWith('/__reload')) {
    handleSSE(req, res);
    return;
  }

  const file = resolveFile(url);

  // Friendly redirect: /foo.md → /foo.html. The markdown sources link to each
  // other with .md (GitHub style), but the site serves generated .html pages —
  // make typed / bookmarked / stale .md URLs resolve to the real page instead
  // of 404ing.
  if (!file && /\.md($|\?)/i.test(url)) {
    const htmlUrl = url.replace(/\.md(?=$|\?)/i, '.html');
    if (resolveFile(htmlUrl)) {
      res.writeHead(302, { Location: htmlUrl, 'Cache-Control': 'no-cache' });
      res.end();
      return;
    }
  }

  if (!file) {
    res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('404 — not found');
    return;
  }

  try {
    serveFile(req, res, file);
  } catch (e) {
    res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('500 — ' + e.message);
  }
});

// Build once, then start.
generateAllDocs();
server.listen(PORT, () => {
  const url = `http://localhost:${PORT}`;
  console.log(`\n📚 Crawler docs running at ${url}`);
  console.log('   Watching docs/ + site/ for changes (live reload on).');
  console.log('   Press Ctrl+C to stop.');
  if (shouldOpen) {
    setTimeout(() => openBrowser(url), 400);
  } else {
    console.log(`   (browser auto-open disabled — visit ${url} manually)`);
  }
  console.log('');
});

try {
  const chokidar = require('chokidar');
  chokidar.watch(DOCS_DIR, { ignored: /node_modules/, ignoreInitial: true })
    .on('change', p => { if (p.endsWith('.md')) rebuildAndReload(); })
    .on('add', p => { if (p.endsWith('.md')) rebuildAndReload(); });
  // Rebuild + reload when static assets (site/) change — generateAllDocs()
  // copies site/ → docs-html/ on every build.
  if (fs.existsSync(SITE_DIR)) {
    chokidar.watch(SITE_DIR, { ignoreInitial: true })
      .on('change', () => rebuildAndReload())
      .on('add', () => rebuildAndReload());
  }
} catch (e) {
  console.log('⚠️  chokidar not found — install dev deps (npm install) for auto-rebuild + live reload.');
}
