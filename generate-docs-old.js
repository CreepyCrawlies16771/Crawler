#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { marked } = require('marked');
const hljs = require('highlight.js');

// Watch mode support
const chokidar = require('chokidar');
const isWatchMode = process.argv.includes('--watch');

// Configure marked with proper highlight function
const highlight = function(code, lang) {
  if (lang && hljs.getLanguage(lang)) {
    return hljs.highlight(code, { language: lang }).value;
  }
  return hljs.highlightAuto(code).value;
};

marked.setOptions({
  breaks: true,
  gfm: true,
  pedantic: false,
  highlight: highlight
});

const DOCS_DIR = path.join(__dirname, 'docs');
const OUTPUT_DIR = path.join(__dirname, 'docs-html');
const INDEX_FILE = path.join(OUTPUT_DIR, 'index.html');

function getTableOfContents() {
  const files = fs.readdirSync(DOCS_DIR)
    .filter(f => f.endsWith('.md') && f !== 'readme.md')
    .sort();
  
  return files.map(file => {
    const name = file.replace('.md', '');
    const htmlFile = name + '.html';
    const title = name
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
    return { name, htmlFile, title, file };
  });
}

function getPageIndex(pageName) {
  const toc = getTableOfContents();
  return toc.findIndex(item => item.name === pageName);
}

function getPreviousPage(pageName) {
  const index = getPageIndex(pageName);
  if (index > 0) {
    const toc = getTableOfContents();
    return toc[index - 1];
  }
  return null;
}

function getNextPage(pageName) {
  const index = getPageIndex(pageName);
  if (index >= 0 && index < getTableOfContents().length - 1) {
    const toc = getTableOfContents();
    return toc[index + 1];
  }
  return null;
}

function createNavigation(currentPage = null) {
  const toc = getTableOfContents();
  let nav = '<nav class="sidebar"><div class="nav-header">📚 Documentation</div><ul class="nav-list">';
  
  nav += '<li><a href="index.html" class="' + (currentPage === 'index' ? 'active' : '') + '">🏠 Home</a></li>';
  
  toc.forEach(item => {
    const isActive = currentPage === item.name ? 'active' : '';
    nav += `<li><a href="${item.htmlFile}" class="${isActive}">${item.title}</a></li>`;
  });
  
  nav += '</ul></nav>';
  return nav;
}

function generateHTMLPage(title, content, isIndex = false) {
  const currentPage = isIndex ? 'index' : title.toLowerCase().replace(/\s+/g, '-');
  const navigation = createNavigation(isIndex ? 'index' : title.toLowerCase().replace(/\s+/g, '-'));
  
  const html = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title} - Crawler Documentation</title>
    <link rel="stylesheet" href="css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/github-dark.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/java.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/json.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/gradle.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/xml.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/markdown.min.js"></script>
    <style>
        .container {
            display: flex;
            max-width: 1400px;
            margin: 0 auto;
            gap: 2rem;
        }
        
        .sidebar {
            position: sticky;
            top: 2rem;
            width: 250px;
            height: fit-content;
            flex-shrink: 0;
        }
        
        .nav-header {
            font-size: 1.2rem;
            font-weight: bold;
            color: var(--primary-green);
            margin-bottom: 1.5rem;
            padding-bottom: 1rem;
            border-bottom: 2px solid var(--border-color);
        }
        
        .nav-list {
            list-style: none;
        }
        
        .nav-list li {
            margin-bottom: 0.5rem;
        }
        
        .nav-list a {
            display: block;
            padding: 0.5rem 1rem;
            border-radius: 4px;
            transition: all 0.2s ease;
            border-left: 3px solid transparent;
        }
        
        .nav-list a:hover {
            background-color: var(--bg-hover);
            border-left-color: var(--primary-green);
        }
        
        .nav-list a.active {
            background-color: var(--bg-hover);
            border-left-color: var(--primary-green);
            color: var(--primary-green);
            font-weight: 600;
        }
        
        .main-content {
            flex: 1;
            min-width: 0;
        }
        
        .content {
            background-color: var(--bg-primary);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 3rem;
            box-shadow: var(--shadow);
        }
        
        .content h1 {
            margin-top: 0;
            padding-bottom: 1rem;
            border-bottom: 2px solid var(--border-color);
            color: var(--text-light);
        }
        
        .content h2 {
            margin-top: 2rem;
            margin-bottom: 1rem;
        }
        
        .content h3 {
            margin-top: 1.5rem;
        }
        
        .content ul, .content ol {
            margin-left: 1.5rem;
            margin-bottom: 1rem;
        }
        
        .content li {
            margin-bottom: 0.5rem;
            color: var(--text-muted);
        }
        
        .content pre {
            background-color: #0d1117;
            border: 2px solid var(--border-color);
            border-radius: 6px;
            padding: 1.5rem;
            overflow-x: auto;
            margin: 1.5rem 0;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
            position: relative;
        }
        
        .content pre::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 2px;
            background: linear-gradient(90deg, var(--primary-green), var(--secondary-green), transparent);
            border-radius: 6px 6px 0 0;
        }
        
        .copy-button {
            position: absolute;
            top: 0.5rem;
            right: 0.5rem;
            background-color: var(--primary-green);
            color: var(--bg-secondary);
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.85rem;
            font-weight: 600;
            transition: all 0.2s ease;
            z-index: 10;
            font-family: 'JetBrains Mono', monospace;
        }
        
        .copy-button:hover {
            background-color: var(--secondary-green);
            transform: scale(1.05);
        }
        
        .copy-button.copied {
            background-color: #4ADE80;
        }
        
        .code-block-wrapper {
            position: relative;
            margin: 1.5rem 0;
        }
        
        .code-block-wrapper pre {
            margin: 0;
        }
        
        .content code {
            background-color: var(--bg-secondary);
            padding: 0.3rem 0.6rem;
            border-radius: 4px;
            color: var(--primary-green);
            font-family: 'JetBrains Mono', 'Monaco', monospace;
            font-size: 0.9em;
            border: 1px solid var(--border-color);
        }
        
        .content pre code {
            background-color: transparent;
            padding: 0;
            color: var(--text-light);
            border: none;
            line-height: 1.5;
            display: block;
            font-family: 'JetBrains Mono', 'Monaco', monospace;
        }
        
        /* Syntax highlighting - GitHub dark theme customization */
        .hljs {
            background-color: transparent !important;
            color: #c9d1d9 !important;
        }
        
        .hljs-attr,
        .hljs-attribute {
            color: #79c0ff !important;
        }
        
        .hljs-string,
        .hljs-literal {
            color: #a5d6ff !important;
        }
        
        .hljs-number {
            color: #79c0ff !important;
        }
        
        .hljs-keyword,
        .hljs-title.class_,
        .hljs-literal {
            color: #ff7b72 !important;
        }
        
        .hljs-name,
        .hljs-type {
            color: #79c0ff !important;
        }
        
        .hljs-function {
            color: #d2a8ff !important;
        }
        
        .hljs-variable {
            color: #c9d1d9 !important;
        }
        
        .hljs-comment {
            color: #8b949e !important;
            font-style: italic;
        }
        
        .hljs-meta,
        .hljs-meta-keyword {
            color: #79c0ff !important;
        }
        
        .hljs-regexp,
        .hljs-symbol {
            color: #a5d6ff !important;
        }
        
        .content blockquote {
            border-left: 4px solid var(--primary-green);
            padding: 1rem;
            margin: 1rem 0;
            background-color: var(--bg-secondary);
            color: var(--text-muted);
            border-radius: 4px;
        }
        
        .content table {
            width: 100%;
            border-collapse: collapse;
            margin: 1rem 0;
            border: 1px solid var(--border-color);
            border-radius: 4px;
            overflow: hidden;
        }
        
        .content table th {
            background-color: var(--bg-secondary);
            color: var(--primary-green);
            padding: 1rem;
            text-align: left;
            font-weight: 600;
            border-bottom: 2px solid var(--border-color);
        }
        
        .content table td {
            padding: 1rem;
            border-bottom: 1px solid var(--border-color);
            color: var(--text-muted);
        }
        
        .content table tr:hover {
            background-color: var(--bg-secondary);
        }
        
        .footer {
            text-align: center;
            padding: 2rem;
            color: var(--text-muted);
            border-top: 1px solid var(--border-color);
            margin-top: 3rem;
        }
        
        @media (max-width: 768px) {
            .container {
                flex-direction: column;
            }
            
            .sidebar {
                position: static;
                width: 100%;
            }
            
            .content {
                padding: 1.5rem;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        ${navigation}
        <main class="main-content">
            <div class="content">
                ${content}
                <div class="footer">
                    <p>Generated from markdown • Last updated: ${new Date().toLocaleDateString()}</p>
                </div>
            </div>
        </main>
    </div>
    <script>
        function copyToClipboard(button) {
            const codeBlock = button.parentElement.querySelector('code');
            const text = codeBlock.textContent;
            
            navigator.clipboard.writeText(text).then(() => {
                // Visual feedback
                button.textContent = '✓ Copied!';
                button.classList.add('copied');
                
                // Reset after 2 seconds
                setTimeout(() => {
                    button.textContent = 'Copy';
                    button.classList.remove('copied');
                }, 2000);
            }).catch(err => {
                console.error('Failed to copy:', err);
                button.textContent = 'Failed';
                setTimeout(() => {
                    button.textContent = 'Copy';
                }, 2000);
            });
        }
        
        document.addEventListener('DOMContentLoaded', (event) => {
            hljs.highlightAll();
        });
    </script>
</body>
</html>`;
  
  return html;
}

function convertMarkdownToHTML(mdFile, mdContent) {
  // Parse markdown to HTML
  let htmlContent = marked.parse(mdContent);
  
  // Add copy buttons to code blocks
  htmlContent = htmlContent.replace(/<pre><code([^>]*)>([\s\S]*?)<\/code><\/pre>/g, (match, attrs, code) => {
    const escapedCode = code.replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&amp;/g, '&');
    return `<div class="code-block-wrapper"><button class="copy-button" onclick="copyToClipboard(this)">Copy</button><pre><code${attrs}>${code}</code></pre></div>`;
  });
  
  return htmlContent;
}

function generateAllDocs() {
  console.log('🔄 Generating documentation...');
  
  // Ensure output directory exists
  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
  }
  
  // Get list of markdown files
  const mdFiles = fs.readdirSync(DOCS_DIR)
    .filter(f => f.endsWith('.md'));
  
  let successCount = 0;
  
  // Convert each markdown file
  mdFiles.forEach(file => {
    const mdPath = path.join(DOCS_DIR, file);
    const htmlFileName = file.replace('.md', '.html');
    const htmlPath = path.join(OUTPUT_DIR, htmlFileName);
    
    try {
      const mdContent = fs.readFileSync(mdPath, 'utf-8');
      const htmlContent = convertMarkdownToHTML(file, mdContent);
      
      // Extract title from first H1 or use filename
      const titleMatch = mdContent.match(/^#\s+(.+)$/m);
      const title = titleMatch ? titleMatch[1] : file.replace('.md', '').replace(/-/g, ' ');
      
      const fullHtml = generateHTMLPage(title, htmlContent, file === 'index.md');
      
      fs.writeFileSync(htmlPath, fullHtml, 'utf-8');
      console.log(`✅ ${file} → ${htmlFileName}`);
      successCount++;
    } catch (error) {
      console.error(`❌ Error processing ${file}:`, error.message);
    }
  });
  
  console.log(`\n✨ Documentation generated! (${successCount}/${mdFiles.length} files)`);
  console.log(`📁 Output: ${OUTPUT_DIR}`);
}

// Initial build
generateAllDocs();

// Watch mode
if (isWatchMode) {
  console.log('\n👀 Watching for changes...');
  const watcher = chokidar.watch(DOCS_DIR, { ignored: /node_modules/ });
  
  watcher.on('change', (filePath) => {
    if (filePath.endsWith('.md')) {
      console.log(`\n📝 ${path.basename(filePath)} changed`);
      generateAllDocs();
    }
  });
  
  watcher.on('add', (filePath) => {
    if (filePath.endsWith('.md')) {
      console.log(`\n📝 ${path.basename(filePath)} added`);
      generateAllDocs();
    }
  });
}
