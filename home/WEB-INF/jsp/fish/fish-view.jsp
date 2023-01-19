<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.mutant.Fish" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="HUMAN_DISEASE" value="Human Disease"/>
<c:set var="EXPRESSION" value="Gene Expression"/>
<c:set var="PHENOTYPE" value="Phenotype"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, HUMAN_DISEASE, EXPRESSION, PHENOTYPE, CITATIONS]}">

    <jsp:attribute name="entityName">
        ${zfn:getTruncatedName(fish.name, 30)}
    </jsp:attribute>

    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
                <nav class="navbar navbar-light admin text-center border-bottom">
                    <a class="col-sm" href="/action/fish/fish-detail/${fish.zdbID}">Old View</a>
                    <a class="col-sm" href="/action/updates/${fish.zdbID}">
                        Last Update:
                        <c:set var="latestUpdate" value="${zfn:getLastUpdate(fish.zdbID)}"/>
                        <c:choose>
                        <c:when test="${!empty latestUpdate}">
                            <fmt:formatDate value="${latestUpdate.dateUpdated}" type="date"/>
                        </c:when>
                        <c:otherwise>
                            Never modified
                        </c:otherwise>
                    </c:choose>
                    </a>
                </nav>
        </authz:authorize>
    </jsp:attribute>
    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">FISH</div>
            <h1>${fish.name}</h1>
            <jsp:include page="fish-view-summary.jsp"/>
        </div>

        <c:set var="modelTitle" value="modelled by"/>
        <c:if test="${fish.clean}">
            <c:set var="modelTitle" value="model utilizes"/>
        </c:if>

        <z:section title="${HUMAN_DISEASE}" infoPopup="/ZFIN/help_files/expression_help.html"
                   appendedText="${modelTitle} ${fish.name}">
            <div class="__react-root" id="FishZebrafishModelTable"
                 data-fish-id="${fish.zdbID}"
            ></div>
        </z:section>

        <z:section title="${EXPRESSION}" infoPopup="/ZFIN/help_files/expression_help.html">
            <z:section title="RNA Expression">
                <div class="__react-root" id="FishRnaExpressionTable"
                     data-fish-id="${fish.zdbID}"
                ></div>
            </z:section>
            <z:section title="Protein Expression">
                <div class="__react-root" id="FishProteinExpressionTable"
                     data-fish-id="${fish.zdbID}"
                ></div>
            </z:section>
            <z:section title="Reporter Gene Expression">
                <div class="__react-root" id="FishReporterExpressionTable"
                     data-fish-id="${fish.zdbID}"
                ></div>
            </z:section>
        </z:section>

        <z:section title="${PHENOTYPE}" infoPopup="/action/marker/note/phenotype">
            <z:section title=" ">
                <div class="__react-root" id="FishPhenotypeTable"
                     data-fish-id="${fish.zdbID}"
                ></div>
            </z:section>
        </z:section>

        <z:section title="${CITATIONS}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${fish.zdbID}"></div>
        </z:section>
    </jsp:body>

</z:dataPage>
