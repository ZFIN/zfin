import { initHeaderMenu } from './header-menu';
import { initYourInputWelcome } from './your-input-welcome';

/*
 * Client-side loader for the shared site "chrome" (header + footer) on
 * statically-served HTML pages (see the zfbook conversion).
 *
 * Normal server-rendered pages already contain the header/footer, so this is a
 * no-op there: it only activates when the page provides the mount points
 *   <div data-zfin-chrome-header></div>
 *   <div data-zfin-chrome-footer></div>
 *
 * The fragments come from ChromeController (/action/layout/header|footer), which
 * runs in Tomcat with the session cookie -- so the injected header reflects live
 * login state. After injecting the header we re-run initHeaderMenu() because the
 * dropdown/mobile/autocomplete handlers bind to elements that did not exist at
 * DOM-ready.
 */
const HEADER_URL = '/action/layout/header';
const FOOTER_URL = '/action/layout/footer';

function inject(mount, html) {
    // Replace the placeholder element with the fetched markup.
    //
    // NB: <script> tags assigned via innerHTML/outerHTML are inert and never run.
    // The chrome fragments legitimately carry scripts -- the header's Sign In
    // redirect helper and the footer's Altcha captcha loader (a type="module"
    // <script>) -- so we parse into an inert <template>, rebuild each <script> as
    // a fresh element (which is NOT flagged "already started"), and insert. The
    // rebuilt scripts then execute when connected to the live document.
    const tpl = document.createElement('template');
    tpl.innerHTML = html;
    tpl.content.querySelectorAll('script').forEach((oldScript) => {
        const script = document.createElement('script');
        for (const attr of Array.from(oldScript.attributes)) {
            script.setAttribute(attr.name, attr.value);
        }
        script.textContent = oldScript.textContent;
        oldScript.replaceWith(script);
    });
    mount.replaceWith(tpl.content);
}

// Client-side cache for the fetched fragments, in localStorage with a 5-minute
// TTL. This is deliberately independent of the HTTP cache, which proved
// unreliable for this response (Tomcat-proxied, chunked, login-aware): Vary:
// Cookie never hits because Google Analytics rewrites its _ga_* cookies every
// page view, and Chrome would not even store a response that Varies on a custom
// request header. Here WE own the cache instead of the browser: on a hit we
// inject with no network request at all.
const CACHE_PREFIX = 'zfin-chrome:';
const CACHE_TTL_MS = 5 * 60 * 1000;

// A per-login-state token stored alongside each cache entry so the cache busts
// automatically on login/logout (the value changes) and is per-session (so a
// shared browser never reuses one user's cached header for the next). The
// `zfin_login` cookie is the natural signal; the token stays in localStorage and
// is never transmitted, so no hashing is needed. Logged out (no cookie) -> "0".
function loginToken() {
    const m = document.cookie.match(/(?:^|;\s*)zfin_login=([^;]+)/);
    return m ? m[1] : '0';
}

async function loadFragment(url) {
    const key = CACHE_PREFIX + url;
    const token = loginToken();

    try {
        const raw = localStorage.getItem(key);
        if (raw) {
            const hit = JSON.parse(raw);
            if (hit && hit.token === token && typeof hit.html === 'string'
                && (Date.now() - hit.ts) < CACHE_TTL_MS) {
                return hit.html; // fresh + same login state -> no network at all
            }
        }
    } catch (e) { /* private mode / bad JSON -> fall through and fetch */ }

    // Miss: fetch fresh. `no-store` bypasses the HTTP cache so a just-logged-in
    // user can never be served a stale logged-out fragment; localStorage is the
    // single source of truth for the TTL window.
    const response = await fetch(url, { credentials: 'same-origin', cache: 'no-store' });
    if (!response.ok) {
        throw new Error(`chrome fetch failed: ${url} -> ${response.status}`);
    }
    const html = await response.text();
    try {
        localStorage.setItem(key, JSON.stringify({ html, ts: Date.now(), token }));
    } catch (e) { /* quota / private mode -> just skip caching */ }
    return html;
}

$(async () => {
    const headerMount = document.querySelector('[data-zfin-chrome-header]');
    const footerMount = document.querySelector('[data-zfin-chrome-footer]');
    if (!headerMount && !footerMount) {
        return; // not a static/chrome page
    }

    try {
        if (headerMount) {
            inject(headerMount, await loadFragment(HEADER_URL));
            initHeaderMenu();
        }
        if (footerMount) {
            inject(footerMount, await loadFragment(FOOTER_URL));
            // The footer fragment carries the "Your Input Welcome" widget, which
            // did not exist at DOM-ready, so bind its handlers now.
            initYourInputWelcome();
        }
    } catch (err) {
        // Header/footer are navigation aids; a failure here must not blank the
        // page content, which is already present in the static HTML.
        console.error('Failed to load page chrome', err);
    }
});
