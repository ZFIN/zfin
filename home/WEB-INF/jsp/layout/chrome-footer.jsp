<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %><%--
    Standalone site footer fragment served at /action/layout/footer and injected
    into statically-served pages by zfin-chrome.js. Renders the same tag files as
    page.tag so there is one source of truth.

    Includes the "Your Input Welcome" widget here because page.tag renders it as a
    sibling right after <z:pageFooter/>; static pages have no page.tag, so folding
    it into the footer fragment is what makes the floating control appear. After
    injecting the footer, zfin-chrome.js re-runs initYourInputWelcome() since the
    widget markup did not exist at DOM-ready.
--%><zfin:clearBuffer/><z:pageFooter/><zfin2:yourInputWelcome/>
