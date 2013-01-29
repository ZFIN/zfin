<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.ontology.presentation.OntologyBean" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<!-- Phenotype section -->
<c:set var="phenotypeSection" value="<%=OntologyBean.Section.PHENOTYPE.toString()%>" scope="page"/>
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
            jQuery('#${phenotypeSection}-mutants').load('/action/anatomy/show-phenotype-mutants/${formBean.term.zdbID}', function() { processPopupLinks(); } );
            jQuery('#${phenotypeSection}-morpholinos').load('/action/anatomy/show-phenotype-wildtype-morpholinos/${formBean.term.zdbID}', function() { processPopupLinks(); });
            jQuery('#${phenotypeSection}-non-wildtype-morpholinos').load('/action/anatomy/show-phenotype-non-wildtype-morpholinos/${formBean.term.zdbID}', function() { processPopupLinks(); });
            showSection('${phenotypeSection}', true);
        }
    </script>
    <div id="${phenotypeSection}-id" style="display:none;">
        <div id="${phenotypeSection}-mutants" class="indented-section"><span
                class="search-result-table-header">Mutants</span> loading
            <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p></p>

        <div id="${phenotypeSection}-morpholinos" class="indented-section"><span
                class="search-result-table-header">Wildtype Morpholinos</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p></p>

        <div id="${phenotypeSection}-non-wildtype-morpholinos" class="indented-section"><span
                class="search-result-table-header">Non-wildtype Morpholinos</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
    </div>
</div>
