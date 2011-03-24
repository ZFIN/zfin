<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<c:set var="expressionSection" value="<%=AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString()%>"
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
    <script type="text/javascript" src="/javascript/prototype.js"></script>
    <script type="text/javascript">
        function show_${expressionSection}() {
            jQuery('#${expressionSection}-genes').load('/action/anatomy/show-expression-genes?zdbID=${formBean.aoTerm.zdbID}', function() { processPopupLinks(); });
            jQuery('#${expressionSection}-inSituProbes').load('/action/anatomy/show-expression-insitu-probes?zdbID=${formBean.aoTerm.zdbID}', function() { processPopupLinks(); });
            jQuery('#${expressionSection}-antibodies').load('/action/anatomy/show-expression-antibodies?zdbID=${formBean.aoTerm.zdbID}', function() { processPopupLinks(); });
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

