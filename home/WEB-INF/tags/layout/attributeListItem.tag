<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="label" required="true" rtexprvalue="true" %>
<%@ attribute name="link" required="false" rtexprvalue="true" %>

<%@ attribute name="dtColSize" required="false" type="java.lang.Integer" %>
<c:set var="dtColSize" value="${(empty dtColSize) ? 2 : dtColSize}" />

<%@ attribute name="ddColSize" required="false" type="java.lang.Integer" %>
<c:set var="ddColSize" value="${(empty ddColSize) ? 12 - dtColSize : ddColSize}" />

<c:choose>
    <c:when test="${link != null}">
        <dt class="col-sm-${dtColSize} mb-sm-2"><a href="${link}">${label}</a></dt>
    </c:when>
    <c:otherwise>
        <dt class="col-sm-${dtColSize} mb-sm-2">${label}</dt>
    </c:otherwise>
</c:choose>
<dd class="col-sm-${ddColSize}"><jsp:doBody /></dd>
