<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="figure" type="org.zfin.expression.Figure" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ABSTRACT" value="Figure Caption"/>
<c:set var="EXPRESSION" value="Expression Data"/>
<c:set var="PHENOTYPE" value="Phenotype Data"/>
<c:set var="ACKNOWLEDGMENTS" value="Acknowledgments"/>

<z:dataPage
        sections="${[SUMMARY, ABSTRACT, EXPRESSION, PHENOTYPE, ACKNOWLEGMENTS]}">

    <jsp:attribute name="entityName">
        ${figure.label}
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerList>
            <a class="dropdown-item" href="/action/figure/view/${figure.zdbID}">Old View</a>
        </z:dataManagerList>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">FIGURE</div>
            <jsp:include page="figure-view-summary.jsp"/>
        </div>

        <z:section title="${ABSTRACT}" appendedText="${figure.label}">
            <zfin-figure:imagesAndCaptionPrototype figure="${figure}"
                                                   showMultipleMediumSizedImages="${showMultipleMediumSizedImages}"/>
        </z:section>

        <z:section title="${EXPRESSION}">
            <zfin-figure:expressionSummaryPrototype summary="${expressionSummary}"/>
            <p/>
            <z:section title="Expression Detail">
                <div class="__react-root" id="FigureExpressionTable" data-figure-id="${figure.zdbID}"></div>
            </z:section>
            <z:section title="Antibody Labeling">
                <div class="__react-root" id="FigureExpressionAntibodyTable" data-figure-id="${figure.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${PHENOTYPE}">
            <zfin-figure:phenotypeSummaryPrototype summary="${phenotypeSummary}"/>
            <p/>
            <z:section title="Phenotype Detail">
                <div class="__react-root" id="FigurePhenotypeTable" data-figure-id="${figure.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${ACKNOWLEGMENTS}">
            <c:choose>
                <c:when test="${figure.publication.canShowImages && figure.publication.type != UNPUBLISHED}">
                    <zfin2:acknowledgment publication="${figure.publication}"
                                          showElsevierMessage="${showElsevierMessage}"
                                          hasAcknowledgment="${hasAcknowledgment}"/>
                </c:when>
                <c:otherwise>
                    <zfin2:subsection>
                        <zfin-figure:journalAbbrev publication="${figure.publication}"/>
                    </zfin2:subsection>
                </c:otherwise>
            </c:choose>
        </z:section>
    </jsp:body>

</z:dataPage>
