<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishSearchFormBean" scope="request"/>

<z:page>
    <zfin-fish:fishSearchFormPage formBean="${formBean}"/>
</z:page>
