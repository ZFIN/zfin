<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %><%--
    Standalone site header fragment served at /action/layout/header and injected
    into statically-served pages by zfin-chrome.js. Renders the same tag file as
    page.tag so there is one source of truth. Buffer is cleared so no leading
    whitespace/newlines from the taglib includes leak into the fragment.
--%><zfin:clearBuffer/><z:pageHeader/>
