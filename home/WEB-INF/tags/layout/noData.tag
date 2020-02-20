<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:doBody var="body" />

<c:if test="${empty body}">
    <c:set var="body" value="No data available" />
</c:if>

<i class="text-muted">${body}</i>
