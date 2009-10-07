<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${!empty formBean.url && !empty formBean.name}">
    <a href="${formBean.url}" target="_blank" class="wiki-link">${formBean.name} Wiki Page</a>
</c:if>
