<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" required="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="markerExpression" required="true" type="org.zfin.expression.presentation.MarkerExpression" %>

<c:set var="hasData" value="${
    (!empty markerExpression.allMarkerExpressionInstance and empty markerExpression.directlySubmittedExpression)
    or
    (markerExpression.allMarkerExpressionInstance.figureCount > markerExpression.directlySubmittedExpression.figureCount)
    or
    (markerExpression.directlySubmittedExpression.figureCount > 0)
}"/>

<z:attributeListItem label="All Expression Data">
    <z:ifHasData test="${hasData}">
        <zfin2:expressionLink marker="${marker}" markerExpression="${markerExpression}"/>
    </z:ifHasData>
</z:attributeListItem>