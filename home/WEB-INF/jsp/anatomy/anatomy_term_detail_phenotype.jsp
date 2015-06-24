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
    <script type="text/javascript">
        function show_${phenotypeSection}() {
            jQuery('#${phenotypeSection}-clean-fish').load('/action/ontology/show-clean-fish/${formBean.term.zdbID}', function () {
                processPopupLinks('#${phenotypeSection}-clean-fish');
            });
/*
            in case we want to display dirty fish as well. For now we decided to hold on to it as the AO page does
            not get much traffic.
            jQuery('#${phenotypeSection}-dirty-fish').load('/action/ontology/show-dirty-fish/${formBean.term.zdbID}', function () {
                processPopupLinks('#${phenotypeSection}-dirty-fish');
            });
*/
            showSection('${phenotypeSection}', true);
        }
    </script>
    <div id="${phenotypeSection}-id" style="display:none;">
        <div id="${phenotypeSection}-clean-fish" class="indented-section"><span
                class="search-result-table-header">Phenotypes caused by Genes</span>
            loading
            <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
<%--
        <p></p>
        <div id="${phenotypeSection}-dirty-fish" class="indented-section"><span
                class="search-result-table-header">Phenotypes Influenced by Experimental Conditions</span>
            loading
            <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
--%>
    </div>
</div>
