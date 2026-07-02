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

async function loadFragment(url) {
    const response = await fetch(url, { credentials: 'same-origin' });
    if (!response.ok) {
        throw new Error(`chrome fetch failed: ${url} -> ${response.status}`);
    }
    return response.text();
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
