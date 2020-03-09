<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:dataTable collapse="true" hasData="${!empty formBean.constructs}">
    <zfin2:toggledLinkList collection="${formBean.constructs}" maxNumber="5" commaDelimited="true"
                           numberOfEntities="${formBean.numberOfConstructs}"
                           ajaxLink="/action/marker/efg/constructs/${formBean.marker.zdbID}" />

</z:dataTable>

