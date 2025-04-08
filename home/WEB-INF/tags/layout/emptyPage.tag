<%@ tag pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@ attribute name="title" rtexprvalue="true" required="false" type="java.lang.String" %>
<%@ attribute name="omitZfinCommonJS" required="false" type="java.lang.Boolean" %>

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

        <c:if test="${!omitZfinCommonJS}">
            <script src="${zfn:getAssetPath("zfin-common.js")}"></script>
        </c:if>

        <!-- Google tag (gtag.js) -->
        <script async src="https://www.googletagmanager.com/gtag/js?id=${GA4_ANALYTICS_ID}"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', '${GA4_ANALYTICS_ID}');
        </script>

    </head>

    <body>
        <main>
            <jsp:doBody/>
        </main>
    </body>
</html>
