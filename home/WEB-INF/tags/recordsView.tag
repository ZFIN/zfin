<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="view" type="java.lang.String" required="true" %>
<%@ attribute name="type" type="java.lang.String" required="true" %>
<%@ attribute name="searchBean" type="org.zfin.profile.presentation.AbstractProfileSearchBean" required="true" %>

<c:choose>
    <c:when test="${formBean.view==view}">
        <span class="current">${view}</span>
    </c:when>
    <c:otherwise>
        <a href="/action/profile/${type}/search/execute?view=${view}&name=${formBean.name}&address=${formBean.address}&contains=${formBean.contains}&containsType=${formBean.containsType}&maxDisplayRecords=${formBean.maxDisplayRecords}&page=${formBean.page}">${view}</a>
    </c:otherwise>
</c:choose>
