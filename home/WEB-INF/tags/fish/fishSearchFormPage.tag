<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="formBean" type="org.zfin.fish.presentation.FishSearchFormBean" required="true" %>

<div class="search-form-top-bar">
    <span class="search-form-title">
        Search for Mutants / Knockdowns / Tg
    </span>
    <a href="/ZFIN/misc_html/fish_search_tips.html" class="popup-link help-popup-link"></a>

    <div class="search-form-your-input-welcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Fish search"/>
        </tiles:insertTemplate>
    </div>

</div>

<zfin-fish:fishSearchForm formBean="${formBean}"/>

