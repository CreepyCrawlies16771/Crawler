/**
 * Crawler Documentation — TUI shell scripts.
 * Vanilla JS: command palette (⌘K), file-tree sidebar, boot sequence,
 * code tabs, theme toggle, copy buttons, scroll reveal, anchors.
 */

document.addEventListener('DOMContentLoaded', function () {
    initializeScripts();
});

function initializeScripts() {
    addHeadingIds();
    initThemeToggle();
    initSidebarDrawer();
    initFileTree();
    initCodeTabs();
    initMdTabs();
    initBootSequence();
    initCommandPalette();
    initScrollReveal();
    smoothScrollLinks();
    initPaletteKbdLabel();
    initHardwarePicker();
    if (window.hljs) hljs.highlightAll();
}

// ============================================
// HARDWARE PICKER — setup page localizer filter
// ============================================

function initHardwarePicker() {
    const picker = document.querySelector('[data-hw-picker]');
    const select = picker && picker.querySelector('[data-hw-select]');
    const hint = picker && picker.querySelector('[data-hw-hint]');
    const panels = document.querySelectorAll('[data-hw-panel]');
    if (!picker || !select || !panels.length) return;

    const STORAGE_KEY = 'crawler-hw';
    const labels = {};
    select.querySelectorAll('option').forEach(function (opt) {
        labels[opt.value] = opt.textContent.trim();
    });

    function apply(value) {
        const v = labels[value] ? value : 'all';
        select.value = v;
        panels.forEach(function (panel) {
            const pv = panel.getAttribute('data-hw-panel');
            const show = v === 'all' || pv === v;
            panel.hidden = !show;
        });
        if (hint) {
            hint.textContent = v === 'all'
                ? 'Pick your hardware to focus the page on just its setup — every localizer\'s full details are below.'
                : 'You picked: ' + labels[v] + ' — the page now shows only that setup. Switch any time.';
        }
        try { localStorage.setItem(STORAGE_KEY, v); } catch (e) { /* ignore */ }
    }

    select.addEventListener('change', function () { apply(select.value); });

    let saved = null;
    try { saved = localStorage.getItem(STORAGE_KEY); } catch (e) { /* ignore */ }
    apply(saved && labels[saved] ? saved : 'all');
}

// Show Ctrl K on non-Mac keyboards.
function initPaletteKbdLabel() {
    if (navigator.platform && /Mac|iPhone|iPad/.test(navigator.platform)) return;
    document.querySelectorAll('.pt-kbd').forEach(function (k) {
        k.textContent = 'Ctrl K';
    });
}

// ============================================
// THEME TOGGLE (dark / light, persisted)
// ============================================

function initThemeToggle() {
    const btn = document.getElementById('themeToggle');
    if (!btn) return;

    btn.addEventListener('click', function () {
        const root = document.documentElement;
        const next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
        root.setAttribute('data-theme', next);
        try {
            localStorage.setItem('crawler-theme', next);
        } catch (e) { /* ignore */ }
    });
}

// ============================================
// SIDEBAR — mobile drawer
// ============================================

function initSidebarDrawer() {
    const toggle = document.getElementById('navToggle');
    const sidebar = document.getElementById('sidebar');
    const layout = sidebar ? sidebar.closest('.layout') : null;
    if (!toggle || !sidebar) return;

    // Mobile drawer (<= 980px) vs desktop auto-collapse (> 980px).
    // On desktop the sidebar auto-hides while scrolling down (content stays
    // centered) and returns on scroll-up or near the top. Clicking ☰ overrides
    // the auto behavior; the override only sticks while it disagrees with what
    // auto would do at the current scroll position, so a second click (or a
    // scroll) returns naturally to auto mode.
    let manual = false;
    let lastY = window.scrollY || 0;
    let scrollAccum = 0;      // cumulative scroll since the last state change
    let ticking = false;
    const HIDE_AFTER = 120;   // px of scroll before the sidebar auto-hides
    const TRIGGER = 10;       // cumulative px needed to flip the state

    function isDesktop() { return window.innerWidth > 980; }

    // The static HTML says aria-expanded="false"; keep it in sync with reality
    // (desktop: sidebar visible = expanded; mobile: drawer open = expanded).
    function syncAria() {
        toggle.setAttribute('aria-expanded', String(isDesktop()
            ? !layout.classList.contains('sidebar-collapsed')
            : sidebar.classList.contains('open')));
    }

    function setOpen(open) {
        sidebar.classList.toggle('open', open);
        if (open && !isDesktop()) sidebar.focus();
        syncAria();
    }

    function setCollapsed(collapsed) {
        if (!layout) return;
        layout.classList.toggle('sidebar-collapsed', collapsed);
        syncAria();
    }

    toggle.addEventListener('click', function () {
        if (isDesktop()) {
            const collapsed = !!layout.classList.contains('sidebar-collapsed');
            setCollapsed(!collapsed);
            // Only keep the override while it disagrees with the auto default.
            const y = window.scrollY || 0;
            manual = !collapsed !== (y > HIDE_AFTER);
            lastY = y;
            scrollAccum = 0;
        } else {
            setOpen(!sidebar.classList.contains('open'));
        }
    });

    document.addEventListener('keydown', function (e) {
        if (e.key !== 'Escape') return;
        if (sidebar.classList.contains('open')) {
            setOpen(false);
        } else if (isDesktop() && layout.classList.contains('sidebar-collapsed')) {
            setCollapsed(false);
            // Keep the override only if auto would hide again at this position.
            const y = window.scrollY || 0;
            manual = y > HIDE_AFTER;
            lastY = y;
            scrollAccum = 0;
        }
    });

    // Close the drawer when navigating to a link inside it.
    sidebar.addEventListener('click', function (e) {
        if (e.target.closest('a') && window.innerWidth <= 980) setOpen(false);
    });

    // Desktop: auto-hide while scrolling down so the content stays centered;
    // return on scroll-up or near the top. The accumulator is clamped to the
    // trigger window, so slow scrolls still trigger reliably and a short
    // scroll in the opposite direction flips the state right away.
    function onScroll() {
        ticking = false;
        if (manual || !isDesktop()) return;
        const y = window.scrollY || 0;
        scrollAccum = Math.max(-TRIGGER, Math.min(TRIGGER, scrollAccum + (y - lastY)));
        lastY = y;
        if (y <= HIDE_AFTER) {
            if (layout.classList.contains('sidebar-collapsed')) setCollapsed(false);
            scrollAccum = 0;
        } else if (scrollAccum >= TRIGGER && !layout.classList.contains('sidebar-collapsed')) {
            setCollapsed(true);
            scrollAccum = 0;
        } else if (scrollAccum <= -TRIGGER && layout.classList.contains('sidebar-collapsed')) {
            setCollapsed(false);
            scrollAccum = 0;
        }
    }

    window.addEventListener('scroll', function () {
        if (!isDesktop()) return;
        if (!ticking) { ticking = true; requestAnimationFrame(onScroll); }
    }, { passive: true });

    window.addEventListener('resize', function () {
        if (!isDesktop()) {
            manual = false;
            if (layout) layout.classList.remove('sidebar-collapsed');
            setOpen(false);
        } else {
            sidebar.classList.remove('open');
            lastY = window.scrollY || 0;
            scrollAccum = 0;
            syncAria();
        }
    });

    syncAria();
}

// ============================================
// FILE TREE — collapsible folders (persisted)
// ============================================

function initFileTree() {
    const folders = document.querySelectorAll('.tree-folder');
    if (!folders.length) return;

    function groupKey(group) { return 'crawler-tree-' + (group || '').toLowerCase().replace(/\s+/g, '-'); }

    folders.forEach(function (folder) {
        const caret = folder.querySelector('.tree-caret');
        const group = folder.getAttribute('data-group') || '';
        if (!caret) return;

        // Restore persisted state.
        try {
            const saved = localStorage.getItem(groupKey(group));
            if (saved === '0') {
                folder.classList.add('collapsed');
                caret.setAttribute('aria-expanded', 'false');
                caret.textContent = '\u25B8';
            }
        } catch (e) { /* ignore */ }

        caret.addEventListener('click', function () {
            const collapsed = folder.classList.toggle('collapsed');
            caret.setAttribute('aria-expanded', collapsed ? 'false' : 'true');
            caret.textContent = collapsed ? '\u25B8' : '\u25BE';
            try {
                localStorage.setItem(groupKey(group), collapsed ? '0' : '1');
            } catch (e) { /* ignore */ }
        });
    });
}

// ============================================
// CODE TABS — adjacent code blocks
// ============================================

function initCodeTabs() {
    document.querySelectorAll('.code-tab').forEach(function (tab) {
        tab.addEventListener('click', function () {
            const container = tab.closest('.code-tabs');
            const paneId = tab.getAttribute('aria-controls');
            const pane = document.getElementById(paneId);
            if (!container || !pane) return;

            container.querySelectorAll('.code-tab').forEach(function (t) {
                const active = t === tab;
                t.classList.toggle('active', active);
                t.setAttribute('aria-selected', active ? 'true' : 'false');
                const target = document.getElementById(t.getAttribute('aria-controls'));
                if (target) target.classList.toggle('hidden', !active);
            });
        });
    });
}

// ============================================
// DOC TABS — markdown %%%tabs blocks
// ============================================

function initMdTabs() {
    const tablists = document.querySelectorAll('.md-tabs');
    if (!tablists.length) return;

    function activate(btn) {
        const group = btn.getAttribute('data-md-group');
        const tablist = btn.closest('.md-tabs');
        tablist.querySelectorAll('.md-tab-btn').forEach(function (b) {
            const on = b === btn;
            b.classList.toggle('active', on);
            b.setAttribute('aria-selected', on ? 'true' : 'false');
            b.setAttribute('tabindex', on ? '0' : '-1');
        });
        document.querySelectorAll('.md-tab-panel[data-md-group="' + group + '"]')
            .forEach(function (panel) {
                panel.classList.toggle('hidden', panel.id !== btn.getAttribute('aria-controls'));
            });
        try {
            history.replaceState(null, '', '#' + btn.getAttribute('aria-controls'));
        } catch (e) { /* ignore */ }
    }

    tablists.forEach(function (tablist) {
        const buttons = Array.prototype.slice.call(tablist.querySelectorAll('.md-tab-btn'));
        buttons.forEach(function (btn) {
            btn.addEventListener('click', function () { activate(btn); });
        });

        // Arrow keys move focus and switch tabs within the strip.
        tablist.addEventListener('keydown', function (e) {
            if (e.key !== 'ArrowLeft' && e.key !== 'ArrowRight') return;
            const idx = buttons.indexOf(document.activeElement);
            if (idx < 0) return;
            e.preventDefault();
            const next = buttons[(idx + (e.key === 'ArrowRight' ? 1 : -1) + buttons.length) % buttons.length];
            next.focus();
            activate(next);
        });
    });

    // Deep-link: open the tab whose panel matches the URL hash (e.g. #md-tab-drive-pid).
    if (location.hash) {
        const target = document.querySelector(location.hash);
        if (target && target.classList.contains('md-tab-panel')) {
            const btn = document.querySelector('.md-tab-btn[aria-controls="' + target.id + '"]');
            if (btn) activate(btn);
        }
    }

    // Clicking an in-page link to a tab panel opens that tab before scrolling.
    document.addEventListener('click', function (e) {
        const a = e.target.closest('a[href^="#"]');
        if (!a) return;
        const href = a.getAttribute('href');
        if (!href || href.length < 2) return;
        const target = document.querySelector(href);
        if (target && target.classList.contains('md-tab-panel')) {
            const btn = document.querySelector('.md-tab-btn[aria-controls="' + target.id + '"]');
            if (btn) activate(btn);
        }
    });
}

// ============================================
// BOOT SEQUENCE — index hero
// ============================================

function initBootSequence() {
    const boot = document.getElementById('bootSeq');
    if (!boot) return;

    const lines = Array.prototype.slice.call(boot.querySelectorAll('.boot-line'));
    const hero = document.getElementById('heroHome');
    if (!lines.length) return;

    const reduceMotion = window.matchMedia &&
        window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (reduceMotion) {
        lines.forEach(function (l) { l.classList.add('shown'); });
        if (hero) hero.classList.add('booted');
        return;
    }

    // Staggered reveal reads like a fast boot log; the command line ends with
    // the blinking block cursor already animating in CSS.
    lines.forEach(function (line, i) {
        setTimeout(function () {
            line.classList.add('shown');
            if (i === lines.length - 1 && hero) hero.classList.add('booted');
        }, 140 + i * 240);
    });
}

// ============================================
// COMMAND PALETTE (⌘K)
// ============================================

function buildPaletteIndex() {
    const items = [];
    document.querySelectorAll('.tree-folder').forEach(function (folder) {
        const group = folder.getAttribute('data-group') || 'docs';
        folder.querySelectorAll('.tree-file').forEach(function (a) {
            const name = (a.textContent || '').trim();
            items.push({
                kind: 'page',
                label: name,
                group: group,
                href: a.getAttribute('href'),
                keywords: (name + ' ' + group + ' ' + name.replace('.md', '').replace(/-/g, ' ')).toLowerCase()
            });
        });
    });

    // Commands
    const editLink = document.querySelector('.sb-edit');
    items.push({ kind: 'command', label: 'Toggle theme', group: 'command', run: 'toggle-theme', keywords: 'theme dark light toggle' });
    items.push({ kind: 'command', label: 'Copy current URL', group: 'command', run: 'copy-url', keywords: 'copy url link share' });
    items.push({ kind: 'command', label: 'Scroll to top', group: 'command', run: 'scroll-top', keywords: 'scroll top beginning' });
    if (editLink) {
        items.push({ kind: 'command', label: 'Edit this page on GitHub', group: 'command', href: editLink.getAttribute('href'), keywords: 'edit github source' });
    }

    return items;
}

function runCommand(run, input) {
    if (run === 'toggle-theme') {
        const btn = document.getElementById('themeToggle');
        if (btn) btn.click();
        return true;
    }
    if (run === 'copy-url') {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(window.location.href).catch(function () {});
        }
        return true;
    }
    if (run === 'scroll-top') {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return true;
    }
    return false;
}

function initCommandPalette() {
    const trigger = document.getElementById('paletteTrigger');
    const backdrop = document.getElementById('paletteBackdrop');
    const input = document.getElementById('paletteInput');
    const results = document.getElementById('paletteResults');
    if (!trigger || !backdrop || !input || !results) return;

    const index = buildPaletteIndex();
    let activeIndex = 0;
    let lastQuery = '';
    let open = false;

    function setOpen(next) {
        open = next;
        if (next) {
            backdrop.hidden = false;
            input.value = '';
            lastQuery = '';
            activeIndex = 0;
            renderResults('');
            requestAnimationFrame(function () { input.focus(); });
            document.dispatchEvent(new CustomEvent('crawler:palette-open'));
        } else {
            backdrop.hidden = true;
            document.dispatchEvent(new CustomEvent('crawler:palette-close'));
        }
    }

    function matchScore(item, q) {
        if (!q) return 1;
        if (item.keywords.indexOf(q) === 0) return 0;
        if (item.keywords.indexOf(' ' + q) !== -1) return 1;
        if (item.keywords.indexOf(q) !== -1) return 2;
        return -1;
    }

    function renderResults(q) {
        lastQuery = q;
        const query = (q || '').trim().toLowerCase();
        let matches = index
            .map(function (item) { return { item: item, score: matchScore(item, query) }; })
            .filter(function (m) { return m.score >= 0; })
            .sort(function (a, b) { return a.score - b.score; })
            .slice(0, 24);

        results.innerHTML = '';
        if (!matches.length) {
            const empty = document.createElement('li');
            empty.className = 'palette-empty';
            empty.textContent = 'no matches for "' + q + '"';
            results.appendChild(empty);
            return;
        }

        matches.forEach(function (m, i) {
            const li = document.createElement('li');
            const a = document.createElement('a');
            a.className = 'palette-result' + (i === activeIndex ? ' active' : '');
            a.setAttribute('role', 'option');
            a.id = 'palette-opt-' + i;
            a.textContent = m.item.label;
            a.dataset.index = i;

            const group = document.createElement('span');
            group.className = 'pr-group';
            group.textContent = m.item.group;
            a.appendChild(group);

            if (m.item.href) {
                a.setAttribute('href', m.item.href);
            } else {
                a.setAttribute('href', '#');
                a.addEventListener('click', function (e) {
                    e.preventDefault();
                    if (!runCommand(m.item.run)) setOpen(false);
                });
            }
            a.addEventListener('mouseenter', function () { setActive(i); });
            li.appendChild(a);
            results.appendChild(li);
        });
        scrollActiveIntoView();
    }

    function setActive(i) {
        activeIndex = i;
        const opts = results.querySelectorAll('.palette-result');
        opts.forEach(function (el, idx) {
            el.classList.toggle('active', idx === i);
        });
        input.setAttribute('aria-activedescendant', activeIndex >= 0 ? 'palette-opt-' + activeIndex : '');
        scrollActiveIntoView();
    }

    function scrollActiveIntoView() {
        const active = results.querySelector('.palette-result.active');
        if (active) active.scrollIntoView({ block: 'nearest' });
    }

    input.addEventListener('input', function () {
        activeIndex = 0;
        renderResults(input.value);
    });

    input.addEventListener('keydown', function (e) {
        const opts = results.querySelectorAll('.palette-result');
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setActive(Math.min(activeIndex + 1, Math.max(opts.length - 1, 0)));
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActive(Math.max(activeIndex - 1, 0));
        } else if (e.key === 'Enter') {
            e.preventDefault();
            const el = opts[activeIndex];
            if (el) el.click();
            if (el && el.getAttribute('href') && el.getAttribute('href') !== '#') setOpen(false);
        } else if (e.key === 'Escape') {
            e.preventDefault();
            setOpen(false);
        }
    });

    trigger.addEventListener('click', function () { setOpen(!open); });
    backdrop.addEventListener('click', function (e) {
        if (e.target === backdrop) setOpen(false);
    });

    // Minimal focus trap — Tab cycles inside the palette while it is open.
    backdrop.addEventListener('keydown', function (e) {
        if (e.key !== 'Tab' || !open) return;
        const focusables = backdrop.querySelectorAll('a[href], button, input, [tabindex]:not([tabindex="-1"])');
        if (!focusables.length) return;
        const first = focusables[0];
        const last = focusables[focusables.length - 1];
        if (e.shiftKey && document.activeElement === first) {
            e.preventDefault();
            last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
            e.preventDefault();
            first.focus();
        }
    });

    document.addEventListener('keydown', function (e) {
        const tag = (e.target && e.target.tagName) || '';
        const typing = tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT';
        if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
            e.preventDefault();
            setOpen(!open);
        } else if (e.key === '/' && !typing && !open) {
            e.preventDefault();
            setOpen(true);
        } else if (e.key === 'Escape' && open) {
            setOpen(false);
        }
    });
}

// ============================================
// COPY CODE (inline onclick target)
// ============================================

function copyToClipboard(button) {
    const pre = button.closest('.code-block').querySelector('pre code');
    if (!pre) return;
    navigator.clipboard.writeText(pre.textContent).then(function () {
        button.textContent = '[copied]';
        button.classList.add('copied');
        setTimeout(function () {
            button.textContent = '[copy]';
            button.classList.remove('copied');
        }, 1800);
    }).catch(function () {
        button.textContent = '[copy failed]';
    });
}
window.copyToClipboard = copyToClipboard;

// ============================================
// SCROLL REVEAL (muted fade/slide)
// ============================================

function initScrollReveal() {
    const els = document.querySelectorAll('.reveal');
    if (!els.length) return;

    const reduceMotion = window.matchMedia &&
        window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (!('IntersectionObserver' in window) || reduceMotion) {
        els.forEach(function (el) { el.classList.add('visible'); });
        return;
    }

    const io = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                io.unobserve(entry.target);
            }
        });
    }, { threshold: 0.12, rootMargin: '0px 0px -30px 0px' });

    els.forEach(function (el) { io.observe(el); });
}

// ============================================
// SMOOTH SCROLL FOR ANCHOR LINKS
// ============================================

function smoothScrollLinks() {
    const links = document.querySelectorAll('a[href^="#"]');
    links.forEach(function (link) {
        link.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (!href || href.length < 2) return;
            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                const offsetTop = target.offsetTop - 90;
                window.scrollTo({ top: offsetTop, behavior: 'smooth' });
            }
        });
    });
}

// ============================================
// ASSIGN HEADING IDS (in-page anchors)
// ============================================

function addHeadingIds() {
    const main = document.querySelector('main');
    if (!main) return;
    main.querySelectorAll('h2, h3, h4').forEach(function (heading, index) {
        if (!heading.id) heading.id = heading.id || 'heading-' + index;
    });
}

// ============================================
// EXPORT FUNCTIONS FOR TESTING
// ============================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        copyToClipboard,
        buildPaletteIndex,
        runCommand
    };
}
