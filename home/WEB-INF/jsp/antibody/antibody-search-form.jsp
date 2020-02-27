<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.antibody.presentation.AntibodySearchFormBean" scope="request"/>

<div class="titlebar">
    <h1>Search for Antibodies</h1>
    <a href="/ZFIN/misc_html/antibody_search_tips.html" class="popup-link help-popup-link"></a>
</div>

<zfin-marker:antibody-search-form formBean="${formBean}"/>
