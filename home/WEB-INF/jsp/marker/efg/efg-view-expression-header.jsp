<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>
<c:set var="markerExpression" value="${formBean.markerExpression}"/>

<z:attributeList>
    <zfin2:expressionAllAttributeListItem marker="${formBean.marker}" markerExpression="${markerExpression}" />
</z:attributeList>