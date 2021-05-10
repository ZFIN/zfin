<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${!empty url && !empty name}">
    <a href="${url}" target="_blank" class="wiki-link">${name} Wiki Page</a>
</c:if>
