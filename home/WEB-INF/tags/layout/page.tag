<!doctype html>

<%@ tag pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@ attribute name="title" rtexprvalue="true" required="false" type="java.lang.String" %>
<%@ attribute name="bodyClass" rtexprvalue="true" required="false" type="java.lang.String" %>
<%@ attribute name="bootstrap" required="false" type="java.lang.Boolean" %>

<c:if test="${empty title}">
    <c:set var="title">
        ZFIN ${dynamicTitle}
    </c:set>
</c:if>

<html lang="en">
    <!-- Server: @INSTANCE@ -->
    <head>
        <meta charset="utf-8">
        <meta http-equiv="x-ua-compatible" content="ie=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${title}</title>

        <link rel="stylesheet" href="${zfn:getAssetPath("style.css")}">

        <!-- jquery is loaded via CDN here instead being part of the webpack bundle so that
         --- bootstrap 4 plays nicely with inline script tags. not sure if this is the best
         --- solution, but it's at least pretty noninvasive !-->
        <!-- jsdelivr is used as the cdn because it works in china !-->
        <script src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
        <script src="${zfn:getAssetPath("vendor-common.js")}"></script>
        <script src="${zfn:getAssetPath("zfin-common.js")}"></script>

        <c:if test="${bootstrap}">
            <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
            <script src="${zfn:getAssetPath("bootstrap.js")}"></script>
        </c:if>

        <script>
            <c:choose>
            <c:when test="${ZfinPropertiesEnum.GOOGLE_ANALYTICS_ID.value() != '0'}">
            !function (z, b, r, f, i, s, h) {
                z.GoogleAnalyticsObject = i, z[i] = z[i] || function () {
                    (z[i].q = z[i].q || []).push(arguments)
                }, z[i].l = +new Date, s = b.createElement(r), h = b.getElementsByTagName(r)[0], s.src = f, h.parentNode.insertBefore(s, h)
            }(this, document, "script", "//www.google-analytics.com/analytics.js", "ga");
            ga('create', '@GOOGLE_ANALYTICS_ID@', {'cookieDomain': 'zfin.org'});
            ga('send', 'pageview');
            </c:when>
            <c:otherwise>
            window.ga = window.ga || function () {
            };
            </c:otherwise>
            </c:choose>
        </script>
    </head>
    <body class="${bodyClass}">
        <z:pageHeader/>
        <main>
            <jsp:doBody/>
        </main>
        <z:pageFooter/>
        <zfin2:yourInputWelcome />
    </body>
</html>
