<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="marker" type="org.zfin.marker.Marker"%>
<%@attribute name="markerExpression" type="org.zfin.expression.presentation.MarkerExpression"%>

<c:choose>
    <c:when test="${markerExpression.allExpressionData.figureCount == 1}">

        ${markerExpression.allExpressionData.singleFigure.link}

    </c:when>
    <c:otherwise>
        <a href="/${marker.zdbID}/expression">
            ${markerExpression.allExpressionData.figureCount} figures
        </a>
    </c:otherwise>
</c:choose>
 from
<c:choose>
    <c:when test="${markerExpression.allExpressionData.publicationCount eq 1}">
        <zfin:link entity="${markerExpression.allExpressionData.singlePublication}"/>
    </c:when>
    <c:otherwise>
        ${markerExpression.allExpressionData.publicationCount} publications
    </c:otherwise>
</c:choose>
