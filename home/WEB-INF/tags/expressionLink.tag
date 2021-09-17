<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="marker" type="org.zfin.marker.Marker" %>
<%@attribute name="markerExpression" type="org.zfin.expression.presentation.MarkerExpression" %>

<c:choose>

    <c:when test="${markerExpression.allMarkerExpressionInstance.figureCount == 1}">

        <a href="/${marker.zdbID}/expression">${markerExpression.allMarkerExpressionInstance.figureCount} figure
        </a>
    </c:when>
    <c:otherwise>
        <a href="/${marker.zdbID}/expression">
                ${markerExpression.allMarkerExpressionInstance.figureCount} figures
        </a>
    </c:otherwise>
</c:choose>
from
<c:choose>
    <c:when test="${markerExpression.allMarkerExpressionInstance.publicationCount eq 1}">
        <zfin:link entity="${markerExpression.allMarkerExpressionInstance.singlePublication}"/>
    </c:when>
    <c:otherwise>
        ${markerExpression.allMarkerExpressionInstance.publicationCount} publications
    </c:otherwise>
</c:choose>
