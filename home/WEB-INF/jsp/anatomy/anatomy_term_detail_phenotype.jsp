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

    <p/>
<%--    see fogbugz 6268, body tag movement kills javascript definition of Ajax --%>
    <script type="text/javascript" src="/javascript/prototype.js"></script>
    <script type="text/javascript">
        function show_${phenotypeSection}() {
            jQuery('#${phenotypeSection}-mutants').load('/action/ontology/show-phenotype-mutants/${formBean.term.zdbID}', function() { processPopupLinks('#${phenotypeSection}-mutants'); } );
            jQuery('#${phenotypeSection}-sequence-targeting-reagents').load('/action/ontology/show-phenotype-wildtype-morpholinos/${formBean.term.zdbID}', function() { processPopupLinks('#${phenotypeSection}-sequence-targeting-reagents'); });
            jQuery('#${phenotypeSection}-non-wildtype-sequence-targeting-reagents').load('/action/ontology/show-phenotype-non-wildtype-morpholinos/${formBean.term.zdbID}', function() { processPopupLinks('#${phenotypeSection}-non-wildtype-sequence-targeting-reagents'); });
            showSection('${phenotypeSection}', true);
        }
    </script>
    <div id="${phenotypeSection}-id" style="display:none;">
        <div id="${phenotypeSection}-mutants" class="indented-section"><span
                class="search-result-table-header">Mutants</span> loading
            <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p></p>

        <div id="${phenotypeSection}-sequence-targeting-reagents" class="indented-section"><span
                class="search-result-table-header">Wildtype Sequence Targeting Reagents</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p></p>

        <div id="${phenotypeSection}-non-wildtype-sequence-targeting-reagents" class="indented-section"><span
                class="search-result-table-header">Non-wildtype Morpholinos</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
    </div>
</div>
