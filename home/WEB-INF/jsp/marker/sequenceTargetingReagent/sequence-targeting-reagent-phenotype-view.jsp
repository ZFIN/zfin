<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:dataTable collapse="true"
             hasData="${formBean.phenotypeDisplays != null && fn:length(formBean.phenotypeDisplays) > 0 }">

</z:dataTable>
