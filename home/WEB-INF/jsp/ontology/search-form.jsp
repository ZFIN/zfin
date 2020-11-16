<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<z:page>
    <zfin-ontology:anatomy-search-form formBean="${formBean}"/>
</z:page>

