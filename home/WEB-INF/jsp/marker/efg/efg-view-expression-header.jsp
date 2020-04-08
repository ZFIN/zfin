<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>
<c:set var="markerExpression" value="${formBean.markerExpression}"/>

<z:attributeList>
    <z:attributeListItem label="All Expression Data:">
        <c:if test="${
                (!empty markerExpression.allExpressionData and empty markerExpression.directlySubmittedExpression)
                or
                (markerExpression.allExpressionData.figureCount > markerExpression.directlySubmittedExpression.figureCount)
                }"><zfin2:expressionLink marker="${formBean.marker}" markerExpression="${markerExpression}"/>
        </c:if>
    </z:attributeListItem>


</z:attributeList>