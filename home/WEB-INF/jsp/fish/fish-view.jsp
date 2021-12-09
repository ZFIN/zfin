<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.mutant.Fish" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="HUMAN_DISEASE" value="Human Disease"/>
<c:set var="EXPRESSION" value="Gene Expression"/>
<c:set var="PHENOTYPE" value="Phenotype"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, HUMAN_DISEASE, EXPRESSION, PHENOTYPE, CITATIONS]}">

    <jsp:attribute name="entityName">
        ${fish.name}
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/curation">Edit</a>
            <a class="dropdown-item" href="/action/fish/fish-detail/${fish.zdbID}">Old View</a>
        </z:dataManagerDropdown>

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
                <zfin2:fish-genotype-phenotype phenotypeDisplays="${phenotypeDisplays}" fishAndCondition="true"
                                               suppressMoDetails="true" secondColumn="condition"/>
            </z:section>
        </z:section>

        <z:section title="${CITATIONS}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${fish.zdbID}"></div>
        </z:section>
    </jsp:body>

</z:dataPage>
