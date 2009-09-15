<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

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
    <script type="text/javascript">
        function show_${expressionSection}() {
            new Ajax.Updater('${expressionSection}-genes', '/action/anatomy/show-expression-genes?zdbID=${formBean.anatomyItem.zdbID}', {Method: 'get'});
            new Ajax.Updater('${expressionSection}-inSituProbes', '/action/anatomy/show-expression-insitu-probes?zdbID=${formBean.anatomyItem.zdbID}', {Method: 'get'});
            new Ajax.Updater('${expressionSection}-antibodies', '/action/anatomy/show-expression-antibodies?zdbID=${formBean.anatomyItem.zdbID}', {Method: 'get'});
            showSection('${expressionSection}', true);
        }
    </script>
    <div id="${expressionSection}-id" style="display:none;">
        <div id="${expressionSection}-genes" class="indented-section"><span class="search-result-table-header">Genes with Most Figures</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p/>

        <div id="${expressionSection}-inSituProbes" class="indented-section"><span class="search-result-table-header">In Situ Probes</span>
            loading <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
        <p/>

        <div id="${expressionSection}-antibodies" class="indented-section"><span class="search-result-table-header">Antibodies</span>
            loading
            <img src="/images/ajax-loader.gif" alt="loading...">
        </div>
    </div>
</div>

