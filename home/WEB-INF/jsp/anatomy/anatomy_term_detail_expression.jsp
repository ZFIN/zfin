<%@ page import="org.zfin.ontology.presentation.OntologyBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<c:set var="expressionSection" value="<%=OntologyBean.Section.EXPRESSION.toString()%>"
       scope="request"/>
<div class="summary">
    <zfin2:sectionVisibility sectionName="${expressionSection}"
                             sectionVisibility="${formBean.sectionVisibility}"
                             showLink="${zfn:dataAvailable(expressionSection, formBean.sectionVisibility)}"
                             enumeration="${formBean.sectionVisibility.visibleSectionsWithData}"
                             hyperlinkName="anatomy-visibility"
                             showAllUsed="false"
                             displaySectionName="EXPRESSION"/>

    <!-- Expression section -->
<%--    see fogbugz 6268, body tag movement kills javascript definition of Ajax --%>
    <script type="text/javascript">
        function show_${expressionSection}() {
            jQuery('#${expressionSection}-genes').load('/action/ontology/show-expressed-genes/${formBean.term.zdbID}');
            jQuery('#${expressionSection}-inSituProbes').load('/action/ontology/show-expressed-insitu-probes/${formBean.term.zdbID}');
            jQuery('#${expressionSection}-antibodies').load('/action/ontology/show-labeled-antibodies/${formBean.term.zdbID}');
            showSection('${expressionSection}', true);
        }
    </script>
    <div id="${expressionSection}-id" style="display:none;">
        <div id="${expressionSection}-genes" class="indented-section"><span class="search-result-table-header">Genes with Most Figures</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p></p>

        <div id="${expressionSection}-inSituProbes" class="indented-section"><span class="search-result-table-header">In Situ Probes</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p></p>

        <div id="${expressionSection}-antibodies" class="indented-section"><span class="search-result-table-header">Antibodies</span>
            loading
            <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
    </div>
</div>

