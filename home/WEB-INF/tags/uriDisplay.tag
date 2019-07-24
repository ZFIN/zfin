<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="uri" type="java.lang.String" required="true" %>

<c:choose>
    <c:when test="${fn:startsWith(uri, 'http:') || fn:startsWith(uri, 'https:')}">
        <a href="${uri}">${uri}</a>
    </c:when>
    <c:otherwise>
        ${uri}
    </c:otherwise>
</c:choose>
