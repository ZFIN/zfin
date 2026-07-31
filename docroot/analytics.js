/*
 * Static Google Analytics (GA4) loader, served by Apache at /analytics.js.
 * Loaded by both sides of the site: the dynamic /action pages (via page.tag /
 * emptyPage.tag) and the statically-served zf_info pages (via their shell).
 *
 * This is ONE committed file used on every instance (no per-instance build step).
 * It selects the GA4 measurement id from the hostname at runtime:
 *
 *   zfin.org / www.zfin.org       -> production property
 *   any other *.zfin.org host     -> non-prod ("zfinlabs") property
 *                                    (stage, dev clones watson/franklin/crick/clone,
 *                                     cell-mac, ...)
 *   localhost / 127.0.0.1         -> non-prod property
 *   anything else (unknown host)  -> NO tracking (never send to the prod property)
 *
 * GA4 measurement ids are client-side/public, so hard-coding them here is fine.
 * This file is the single source of truth for them: the dynamic /action site
 * loads /analytics.js too (via page.tag / emptyPage.tag), and the former
 * per-instance GA4_ANALYTICS_ID* properties in commons/env have been retired.
 */
(function () {
    var PROD_ID = 'G-R5XJW0QW0Y';     // production (zfin.org / www.zfin.org)
    var NONPROD_ID = 'G-5J7RMKMBWC';  // non-prod "zfinlabs" (stage / dev / localhost)

    var host = window.location.hostname;
    var id;
    if (host === 'zfin.org' || host === 'www.zfin.org') {
        id = PROD_ID;
    } else if (/\.zfin\.org$/.test(host) || host === 'localhost' || host === '127.0.0.1') {
        id = NONPROD_ID;
    } else {
        return; // unknown host -> no analytics (never touches the production property)
    }

    var gtagScript = document.createElement('script');
    gtagScript.async = true;
    gtagScript.src = 'https://www.googletagmanager.com/gtag/js?id=' + encodeURIComponent(id);
    document.head.appendChild(gtagScript);

    window.dataLayer = window.dataLayer || [];
    function gtag() { window.dataLayer.push(arguments); }
    window.gtag = gtag;
    gtag('js', new Date());
    gtag('config', id);
})();
