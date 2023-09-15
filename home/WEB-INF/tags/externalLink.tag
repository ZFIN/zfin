<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="href" required="true" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="newTab" required="false" type="java.lang.Boolean" rtexprvalue="true" %>
<%@ attribute name="className" required="false" rtexprvalue="true" %>
<%@ attribute name="id" required="false" rtexprvalue="true" %>

<c:if test="${empty newTab}">
    <c:set var="newTab" value="true" />
</c:if>

<c:choose>
    <c:when test="${not empty id}">
<a href="${href}" class="external ${className}" id="${id}" target="${newTab ? '_blank' : '_self'}" rel="${newTab ? 'noopener noreferrer' : ''}"><jsp:doBody /></a>
    </c:when>
    <c:otherwise>
<a href="${href}" class="external ${className}" target="${newTab ? '_blank' : '_self'}" rel="${newTab ? 'noopener noreferrer' : ''}"><jsp:doBody /></a>
    </c:otherwise>
</c:choose>