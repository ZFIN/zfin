<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" required="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="markerExpression" required="true" type="org.zfin.expression.presentation.MarkerExpression" %>

<c:set var="hasData" value="${
    (!empty markerExpression.allExpressionData and empty markerExpression.directlySubmittedExpression)
    or
    (markerExpression.allExpressionData.figureCount > markerExpression.directlySubmittedExpression.figureCount)
}"/>

<z:attributeListItem label="All Expression Data">
    <z:ifHasData test="${hasData}">
        <zfin2:expressionLink marker="${marker}" markerExpression="${markerExpression}"/>
    </z:ifHasData>
</z:attributeListItem>