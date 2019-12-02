<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="test" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="noDataMessage" required="false" rtexprvalue="true" %>

<c:choose>
    <c:when test="${empty test || test == true}">
        <jsp:doBody />
    </c:when>
    <c:otherwise>
        <zfin-prototype:noData>${noDataMessage}</zfin-prototype:noData>
    </c:otherwise>
</c:choose>