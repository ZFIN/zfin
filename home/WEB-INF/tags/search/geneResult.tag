<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult" required="true" %>

<zfin-search:resultTemplate result="${result}">
    <jsp:attribute name="metadata">${result.category}</jsp:attribute>
    <jsp:body>
        <c:set var="expressionModalDomID" value="${result.id}-gene-expression-modal"/>
        <c:set var="phenotypeModalDomID" value="${result.id}-phenotype-modal"/>

        <%--todo: only do this for type:gene--%>

<%--
        <a href="/action/quicksearch/gene-expression/${result.id}" data-toggle="modal" data-target="#${expressionModalDomID}">Expression</a>
--%>

        <div id="${expressionModalDomID}" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <span class="result-header search-result-name">
                   <span class="genedom">${result.name}</span>
                   Wildtype Expression
                </span>
            </div>
            <div class="modal-body">
                <img src="/images/ajax-loader.gif"/>
            </div>
            <div class="modal-footer">
                <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
            </div>
        </div>

        <div id="${phenotypeModalDomID}" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <span class="result-header search-result-name">
                   <span class="genedom">${result.name}</span>
                   Phenotype
                </span>
            </div>
            <div class="modal-body">
                <img src="/images/ajax-loader.gif"/>
            </div>
            <div class="modal-footer">
                <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
            </div>
        </div>


    </jsp:body>
</zfin-search:resultTemplate>


