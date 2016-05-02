<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult" required="true" %>

<zfin-search:resultTemplate result="${result}">
    <jsp:attribute name="metadata">${result.category}</jsp:attribute>
    <jsp:body>
        <c:set var="expressionModalDomID" value="${result.id}-gene-expression-modal"/>
        <c:set var="phenotypeModalDomID" value="${result.id}-phenotype-modal"/>

        <div id="${expressionModalDomID}" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div style="padding: 5em;">
                        Loading... <img src="/images/ajax-loader.gif"/>
                    </div>
                </div>
            </div>
        </div>

        <div id="${phenotypeModalDomID}" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div style="padding: 5em;">
                        Loading... <img src="/images/ajax-loader.gif"/>
                    </div>
                </div>
            </div>
        </div>

        <script>
            $('#${phenotypeModalDomID}').appendTo('body');
            $('#${expressionModalDomID}').appendTo('body');
        </script>

    </jsp:body>
</zfin-search:resultTemplate>


