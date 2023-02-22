<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="figure" type="org.zfin.expression.Figure" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="FIGURE_CAPTION" value="Figure Caption"/>
<c:set var="EXPRESSION" value="Expression Data"/>
<c:set var="PHENOTYPE" value="Phenotype Data"/>
<c:set var="ACKNOWLEDGMENTS" value="Acknowledgments"/>

<z:dataPage
        sections="${[SUMMARY, FIGURE_CAPTION, EXPRESSION, PHENOTYPE, ACKNOWLEDGMENTS]}">

    <jsp:attribute name="entityName">
        ${figure.label}
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerList>
            <a class="dropdown-item" href="/action/figure/view/${figure.zdbID}">Old View</a>
        </z:dataManagerList>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">FIGURE</div>
            <h1>${figure.label}</h1>
            <jsp:include page="figure-view-summary.jsp"/>
        </div>

        <z:section title="${figure.label}" sectionID="${zfn:makeDomIdentifier(FIGURE_CAPTION)}">
            <zfin-figure:imagesAndCaption
                    figure="${figure}"
                    autoplayVideo="false"
                    showMultipleMediumSizedImages="${showMultipleMediumSizedImages}"
                    showCaption="true"></zfin-figure:imagesAndCaption>
        </z:section>

        <z:section title="${EXPRESSION}">
            <zfin-figure:expressionSummaryPrototype summary="${expressionSummary}"/>
            <p/>
            <z:section title="Expression Detail">
                <div class="__react-root" id="FigureExpressionTable" data-hide-figure-column="true"
                     data-url="/action/api/figure/${figure.zdbID}/expression-detail"></div>
            </z:section>
            <z:section title="Antibody Labeling">
                <div class="__react-root" id="FigureExpressionAntibodyTable" data-figure-id="${figure.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${PHENOTYPE}">
            <zfin-figure:phenotypeSummaryPrototype summary="${phenotypeSummary}"/>
            <p/>
            <z:section title="Phenotype Detail">
                <div class="__react-root" id="FigurePhenotypeTable" data-hide-figure-column="true"
                     data-url="/action/api/figure/${figure.zdbID}/phenotype-detail"></div>
            </z:section>
        </z:section>

        <z:section title="${ACKNOWLEDGMENTS}">
            <c:choose>
                <c:when test="${figure.publication.canShowImages && figure.publication.type != UNPUBLISHED}">
                    <zfin2:acknowledgment-text publication="${figure.publication}"
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
