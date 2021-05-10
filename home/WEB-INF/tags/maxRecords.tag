<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="count" type="java.lang.Integer" required="true" %>
<%@ attribute name="searchType" type="java.lang.String" required="true" %>
<%@ attribute name="searchBean" type="org.zfin.profile.presentation.AbstractProfileSearchBean" required="true" %>

<%--<c:if test="${searchBean.totalRecords >= count}">--%>
<c:choose>
    <c:when test="${searchBean.maxDisplayRecords==count}">
        <span class="current">${count}</span>
    </c:when>
    <c:otherwise>
        <a href="/action/profile/${searchType}/search/execute?view=${searchBean.view}&name=${searchBean.name}&address=${searchBean.address}&contains=${searchBean.contains}&containsType=${searchBean.containsType}&maxDisplayRecords=${count}">${count}</a>
    </c:otherwise>
</c:choose>
<%--</c:if>--%>
