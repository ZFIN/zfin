<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="RUN" value="Run Indexer"/>

<z:dataPage sections="${[SUMMARY, RUN]}">
    <jsp:attribute name="entityName">
    </jsp:attribute>

    <jsp:body>
        <z:section title="Indexer Dashboard">
            <z:section>
                <div class="__react-root" id="IndexerRunTable"></div>
            </z:section>
        </z:section>

        <z:section title="Run Indexer">
            <z:section>
                <div class="__react-root" id="RunIndexerTable"></div>
            </z:section>
        </z:section>

    </jsp:body>
</z:dataPage>

