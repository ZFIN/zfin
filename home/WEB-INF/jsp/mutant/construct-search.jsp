<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.mutant.presentation.ConstructSearchFormBean" scope="request"/>


<zfin-mutant:constructSearchFormPage formBean="${formBean}"/>
