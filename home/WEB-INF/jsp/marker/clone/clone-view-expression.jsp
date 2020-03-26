<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%--<z:dataTable hasData="${!empty formBean.markerExpression}">--%>
    <z:attributeList>
    <zfin2:markerExpressionClone markerExpression="${formBean.markerExpression}" marker="${formBean.marker}"/>
    </z:attributeList>
<%--</z:dataTable>--%>