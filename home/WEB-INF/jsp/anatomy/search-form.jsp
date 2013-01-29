<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin-ontology:anatomy-search-form formBean="${formBean}"/>

<authz:authorize ifAnyGranted="root">
    <a href="anatomy-expression-search">Expression Report</a>
    ||
    <a href="anatomy-phenotype-search">Phenotype Report</a>
    ||
    <a href="anatomy-go-evidence-search">Go Evidence Report</a>
</authz:authorize>

