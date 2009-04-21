<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>

<!-- Phenotype section -->
<c:set var="phenotypeSection" value="<%=AnatomySearchBean.Section.ANATOMY_PHENOTYPE.toString()%>" scope="page"/>
<div class="summary">
    <zfin2:sectionVisibility sectionName="${phenotypeSection}"
                             sectionVisibility="${formBean.sectionVisibility}"
                             showLink="${zfn:dataAvailable(phenotypeSection, formBean.sectionVisibility)}"
                             enumeration="${formBean.sectionVisibility.visibleSectionsWithData}"
                             hyperlinkName="anatomy-visibility"
                             showAllUsed="false"
                             displaySectionName="PHENOTYPE"/>

    <p/>
    <script type="text/javascript">
        function show_${phenotypeSection}() {
            new Ajax.Updater('${phenotypeSection}-mutants', '/action/anatomy/show-phenotype-mutants?zdbID=${formBean.anatomyItem.zdbID}', {Method: 'get' });
            new Ajax.Updater('${phenotypeSection}-morpholinos', '/action/anatomy/show-phenotype-morpholinos?zdbID=${formBean.anatomyItem.zdbID}', {Method: 'get' });
            new Ajax.Updater('${phenotypeSection}-non-wildtype-morpholinos', '/action/anatomy/show-phenotype-non-wildtype-morpholinos?zdbID=${formBean.anatomyItem.zdbID}', {Method: 'get' });
            showSection('${phenotypeSection}', true);
        }
    </script>
    <div id="${phenotypeSection}-id" style="display:none; text-indent:20pt">
        <div id="${phenotypeSection}-mutants" class="indented-section"><span
                class="search-result-table-header">Mutants</span> loading
            <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p/>

        <div id="${phenotypeSection}-morpholinos" class="indented-section"><span
                class="search-result-table-header">Wildtype Morpholinos</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p/>

        <div id="${phenotypeSection}-non-wildtype-morpholinos" class="indented-section"><span
                class="search-result-table-header">Non-wildtype Morpholinos</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
    </div>
</div>
