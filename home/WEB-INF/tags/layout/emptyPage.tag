<%@ tag pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@ attribute name="title" rtexprvalue="true" required="false" type="java.lang.String" %>
<%@ attribute name="omitZfinCommonJS" required="false" type="java.lang.Boolean" %>

<c:set var="INSTANCE" value="${ZfinPropertiesEnum.INSTANCE.value()}" />
<c:if test="${empty INSTANCE}">
    <c:set var="INSTANCE" value="Unknown Instance" />
</c:if>

<c:if test="${empty title}">
    <c:set var="title">
        ZFIN ${dynamicTitle}
    </c:set>
</c:if>

<%-- Clear the buffer before rendering the page --%>
<zfin:clearBuffer/><!doctype html>
<html lang="en">
    <!-- Server: ${INSTANCE} -->
    <head>
        <meta charset="utf-8">
        <meta http-equiv="x-ua-compatible" content="ie=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${title}</title>

        <c:if test="${!omitZfinCommonJS}">
            <script src="${zfn:getAssetPath("zfin-common.js")}"></script>
        </c:if>

        <!-- Analytics: single host-switched loader (same /analytics.js the static
             pages use) -- picks the GA4 id by hostname; one source of truth. -->
        <script src="/analytics.js"></script>

    </head>

    <body>
        <main>
            <jsp:doBody/>
        </main>
    </body>
</html>
