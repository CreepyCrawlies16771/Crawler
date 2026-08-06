/**
 * Crawler Docs — Canvas UI mount module.
 *
 * Wires the three Canvas UI (canvasui.dev) vanilla components into the docs
 * shell, keeping them out of the critical text-rendering path:
 *
 *   - ParticleReveal → index hero wordmark (assembles from particles)
 *   - Glass          → command-palette pane (floating glass)
 *   - Grid           → faint full-bleed background on API reference pages
 *
 * Everything degrades gracefully. The html-in-canvas capture API is currently
 * a Chrome flag (chrome://flags/#canvas-draw-element) or origin trial; without
 * it the page content simply renders as plain HTML and the effect canvases are
 * removed. Components pause off-screen and respect prefers-reduced-motion.
 */

import {
  createParticleReveal,
  supportsHtmlInCanvas as supportsParticleReveal,
} from './canvasui/ParticleRevealVanilla.js';
import {
  createGlass,
  supportsHtmlInCanvas as supportsGlass,
} from './canvasui/GlassVanilla.js';
import {
  createGrid,
  supportsHtmlInCanvas as supportsGrid,
} from './canvasui/GridVanilla.js';

const PREFERS_REDUCED =
  window.matchMedia &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches;

function safe(label, fn) {
  try {
    fn();
  } catch (err) {
    console.warn('[crawler canvasui] ' + label, err);
  }
}

// ---------------------------------------------------------------------------
// ParticleReveal — index wordmark
// ---------------------------------------------------------------------------

function initParticleReveal() {
  const stage = document.getElementById('prStage');
  const source = document.getElementById('prSource');
  const content = document.getElementById('prContent');
  const output = document.getElementById('prOutput');
  if (!stage || !source || !content || !output) return;

  if (!supportsParticleReveal() || PREFERS_REDUCED) {
    // Wordmark stays as plain HTML; drop the capture/effect canvases.
    source.remove();
    output.remove();
    return;
  }

  stage.classList.add('cu-pr-active');

  // The capture API needs the content inside the layoutsubtree canvas.
  const rect = content.getBoundingClientRect();
  source.appendChild(content);
  source.style.width = Math.max(1, Math.round(rect.width)) + 'px';
  source.style.height = Math.max(1, Math.round(rect.height)) + 'px';

  safe('ParticleReveal', () => {
    createParticleReveal(
      { source, content, output },
      {
        radius: 110, // reveal radius around the cursor, CSS px
        softness: 0.8,
        size: 1.4,
        scatter: 12,
        drift: 0.45, // gentle idle shimmer — restrained, no glow
        aberration: 10,
        bend: 14,
        fade: 0.9,
        background: '#0a0a0a',
        smoothing: 0.2,
      }
    );
  });
}

// ---------------------------------------------------------------------------
// Glass — command-palette pane
// ---------------------------------------------------------------------------

let glassInstance = null;

function mountGlass() {
  const host = document.getElementById('glassHost');
  const output = document.getElementById('glassOutput');
  if (!host || !output) return;

  if (!supportsGlass() || PREFERS_REDUCED) {
    output.remove();
    return;
  }

  const source = document.createElement('canvas');
  source.setAttribute('layoutsubtree', '');
  source.className = 'glass-source';
  host.insertBefore(source, host.firstChild);
  host.classList.add('cu-glass-active');

  safe('Glass', () => {
    glassInstance = createGlass(
      { source, content: host, output },
      {
        shape: 'rectangle',
        size: 460, // half-height of the lens, CSS px
        aspect: 2.2,
        corner: 22,
        ior: 1.2, // close to air — glass stays optically honest
        edge: 0.8,
        bevel: 2,
        depth: 16,
        aberration: 0.4,
        blur: 0.4, // slight frost so it reads as a pane, not a magnifier
        reflection: 0.9,
        shine: 0.6,
        follow: 0.12,
      }
    );
  });
}

function initGlass() {
  // The palette is display:none until first opened, so mount lazily on first
  // open and resize on every subsequent open.
  mountGlass();
  document.addEventListener('crawler:palette-open', () => {
    if (glassInstance) glassInstance.resize();
  });
}

// ---------------------------------------------------------------------------
// Grid — faint background on API reference pages
// ---------------------------------------------------------------------------

function initGrid() {
  if (!document.body.classList.contains('api-page')) return;

  const source = document.createElement('canvas');
  source.setAttribute('layoutsubtree', '');
  source.className = 'grid-source';
  const output = document.createElement('canvas');
  output.className = 'grid-bg';

  document.body.appendChild(source);
  document.body.appendChild(output);

  safe('Grid', () => {
    createGrid(
      { source, content: document.body, output },
      {
        tileSize: 56,
        gap: 1,
        cornerRadius: 4,
        amplitude: 1.1, // faint — background must never fight the text
        waveSpeed: 0.55,
        frequency: 4,
        waveWidth: 0.18,
        fadeTime: 2.2,
        maxLift: 0.28,
        jitter: 0.3,
        liftHeight: 6,
        perspective: 1000,
        tilt: 0.15,
        shading: 0.4,
        tint: [0.13, 0.55, 0.38],
        tintStrength: 0.22,
        idleRipples: 0, // no ambient motion — cursor-driven only
      }
    );
  });
}

// ---------------------------------------------------------------------------

function init() {
  initParticleReveal();
  initGlass();
  initGrid();
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}

export { init };
