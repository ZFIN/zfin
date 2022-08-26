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

        <z:section title="${HUMAN_DISEASE}" infoPopup="/ZFIN/help_files/expression_help.html"
                   appendedText="modelled by ${fish.name}">
            <z:section title="">
                <jsp:include page="fish-view-human-disease.jsp"/>
            </z:section>
        </z:section>

        <z:section title="${EXPRESSION}" infoPopup="/ZFIN/help_files/expression_help.html">
            <z:section title="RNA Expression">
                <zfin2:fishExpressionData fishZdbID="${fish.zdbID}"
                                          expressionDisplays="${geneCentricNonEfgExpressionDataList}"
                                          showCondition="true"/>
            </z:section>
            <z:section title="Protein Expression">
                <jsp:include page="fish-view-protein-detail.jsp"/>
            </z:section>
            <z:section title="Reporter Gene Expression">
                <zfin2:fishExpressionData fishZdbID="${fish.zdbID}"
                                          expressionDisplays="${geneCentricEfgExpressionDataList}"
                                          showCondition="true"/>
            </z:section>
        </z:section>

        <z:section title="${PHENOTYPE}" infoPopup="/action/marker/note/phenotype">
            <z:section title=" ">
                <zfin2:all-phenotype phenotypeDisplays="${phenotypeDisplays}" fishAndCondition="true"
                                     suppressMoDetails="true" secondColumn="condition"/>
            </z:section>
        </z:section>

        <z:section title="${CITATIONS}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${fish.zdbID}"></div>
        </z:section>
    </jsp:body>

</z:dataPage>
