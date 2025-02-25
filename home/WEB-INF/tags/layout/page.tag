<%@ tag pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ tag import="org.zfin.framework.featureflag.FeatureFlagEnum" %>

<%@ attribute name="title" rtexprvalue="true" required="false" type="java.lang.String" %>
<%@ attribute name="bodyClass" rtexprvalue="true" required="false" type="java.lang.String" %>
<%@ attribute name="bootstrap" required="false" type="java.lang.Boolean" %>

<%@ attribute name="additionalBodyClass" required="false" type="java.lang.String" %>
<c:set var="additionalBodyClass" value="${(empty additionalBodyClass) ? '' : additionalBodyClass}" />

<c:set var="GA4_ANALYTICS_ID" value="${ZfinPropertiesEnum.GA4_ANALYTICS_ID.value()}" />
<c:if test="${empty GA4_ANALYTICS_ID}">
    <c:set var="GA4_ANALYTICS_ID" value="0" />
</c:if>

<c:if test="${empty title}">
    <c:set var="title">
        ZFIN ${dynamicTitle}
    </c:set>
</c:if>

<%-- Clear the buffer before rendering the page --%>
<zfin:clearBuffer/><!doctype html>
<html lang="en">
    <!-- Server: @INSTANCE@ -->
    <head>
        <meta charset="utf-8">
        <meta http-equiv="x-ua-compatible" content="ie=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${title}</title>

        <c:if test="${not empty canonicalUrl}">
            <link rel="canonical" href="${canonicalUrl}" />
        </c:if>

        <link rel="stylesheet" href="${zfn:getAssetPath("style.css")}">
<%--
        jquery is loaded via CDN here instead being part of the webpack bundle so that
        bootstrap 4 plays nicely with inline script tags. not sure if this is the best
        solution, but it's at least pretty noninvasive
        jsdelivr is used as the cdn because it works in china --%>
        <script src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
        <script src="${zfn:getAssetPath("vendor-common.js")}"></script>
        <script src="${zfn:getAssetPath("zfin-common.js")}"></script>

        <c:if test="${bootstrap}">
            <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
            <script src="${zfn:getAssetPath("bootstrap.js")}"></script>
        </c:if>

        <script>
            !function (z, b, r, f, i, s, h) {
                z.GoogleAnalyticsObject = i, z[i] = z[i] || function () {
                    (z[i].q = z[i].q || []).push(arguments)
                }, z[i].l = +new Date, s = b.createElement(r), h = b.getElementsByTagName(r)[0], s.src = f, h.parentNode.insertBefore(s, h)
            }(this, document, "script", "//www.google-analytics.com/analytics.js", "ga");
            ga('send', 'pageview');
        </script>

        <c:if test="${zfn:isFlagEnabled(FeatureFlagEnum.USE_GA4_ANALYTICS)}">
            <!-- Google tag (gtag.js) -->
            <c:choose>
                <c:when test="${GA4_ANALYTICS_ID != '0'}">
                    <script async src="https://www.googletagmanager.com/gtag/js?id=${GA4_ANALYTICS_ID}"></script>
                    <script>
                        window.dataLayer = window.dataLayer || [];
                        function gtag(){dataLayer.push(arguments);}
                        gtag('js', new Date());

                        gtag('config', '${GA4_ANALYTICS_ID}');
                    </script>
                </c:when>
                <c:otherwise>
                    <!-- No GA4_ANALYTICS_ID set -->
                </c:otherwise>
            </c:choose>
        </c:if>

    </head>
<%--
    <div class="uber-banner">
<pre>
Community Action Needed: Please respond to the <a href="https://zfin.atlassian.net/wiki/spaces/news/blog/2021/09/23/4776525828/NIH+request+for+information+on+Scientific+Data+Sources">NIH RFI</a>
</pre>
        </div>
--%>
    <body class="${bodyClass} ${additionalBodyClass}">
        <z:pageHeader/>
        <main>
            <jsp:doBody/>
        </main>
        <z:pageFooter/>
        <zfin2:yourInputWelcome />
    </body>
</html>
