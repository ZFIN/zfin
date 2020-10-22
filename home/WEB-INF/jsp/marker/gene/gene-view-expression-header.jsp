<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>
<c:set var="markerExpression" value="${formBean.markerExpression}"/>

<z:attributeList>
    <zfin2:expressionAllAttributeListItem marker="${formBean.marker}" markerExpression="${markerExpression}" />
    <zfin2:expressionCrossSpeciesAttributeListItem marker="${formBean.marker}" bgeeIdList="${bGeeIds}" />
    <zfin2:expressionHighThroughputAttributeListItem markerExpression="${markerExpression}" />
</z:attributeList>