<!doctype html>

<%@ tag pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ attribute name="title" %>

<html lang="en">
<!-- Server: @INSTANCE@ -->
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>${title}</title>

    <link rel="stylesheet" href="${zfn:getAssetPath("style.css")}">

    <script src="${zfn:getAssetPath("vendor-common.js")}"></script>
    <script src="${zfn:getAssetPath("zfin-common.js")}"></script>

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

        $(function () {
            processPopupLinks('body');
            $(".default-input").focus();
        });
    </script>
</head>
<body>
<zfin2:pageHeader />
<main>
    <jsp:doBody/>
</main>
<zfin2:pageFooter />
</body>
</html>
