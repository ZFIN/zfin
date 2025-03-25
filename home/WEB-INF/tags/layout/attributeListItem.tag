<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="label" required="true" rtexprvalue="true" %>
<%@ attribute name="link" required="false" rtexprvalue="true" %>
<%@ attribute name="statsLink" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="copyable" required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<%@ attribute name="dtColSize" required="false" type="java.lang.Integer" %>
<c:set var="dtColSize" value="${(empty dtColSize) ? 2 : dtColSize}" />

<%@ attribute name="ddColSize" required="false" type="java.lang.Integer" %>
<c:set var="ddColSize" value="${(empty ddColSize) ? 12 - dtColSize : ddColSize}" />

<c:choose>
    <c:when test="${link != null}">
        <dt class="col-sm-${dtColSize} mb-sm-2 attribute-list-item-dt"><a href="${link}">${label}</a></dt>
    </c:when>
    <c:otherwise>
        <dt class="col-sm-${dtColSize} mb-sm-2 attribute-list-item-dt">${label} <c:if test="${not empty statsLink}"><a class="popup-link info-popup-link" href="/action/publication/stats/view?section=${statsLink}"></a></c:if></dt>
    </c:otherwise>
</c:choose>

<dd class="col-sm-${ddColSize} mb-sm-${ddColSize} attribute-list-item-dd">
    <c:if test="${empty copyable}">
        <jsp:doBody/>
    </c:if>
    <c:if test="${not empty copyable}">
        <span class="__react-root" id="CopyTarget"><jsp:doBody/></span>
    </c:if>
</dd>
