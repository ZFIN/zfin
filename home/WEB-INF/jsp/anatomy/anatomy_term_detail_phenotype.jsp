<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

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

    <p></p>
<%--    see fogbugz 6268, body tag movement kills javascript definition of Ajax --%>
    <script type="text/javascript" src="/javascript/prototype.js"></script>
    <script type="text/javascript">
        function show_${phenotypeSection}() {
            new Ajax.Updater('${phenotypeSection}-mutants', '/action/anatomy/show-phenotype-mutants?zdbID=${formBean.aoTerm.zdbID}', {Method: 'get' });
            new Ajax.Updater('${phenotypeSection}-morpholinos', '/action/anatomy/show-phenotype-morpholinos?zdbID=${formBean.aoTerm.zdbID}', {Method: 'get' });
            new Ajax.Updater('${phenotypeSection}-non-wildtype-morpholinos', '/action/anatomy/show-phenotype-non-wildtype-morpholinos?zdbID=${formBean.aoTerm.zdbID}', {Method: 'get' });
            showSection('${phenotypeSection}', true);
        }
    </script>
    <div id="${phenotypeSection}-id" style="display:none;">
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
