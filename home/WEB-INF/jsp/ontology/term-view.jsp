<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>
<c:set var="term" value="${formBean.term}"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="RELATIONSHIP" value="Relationships"/>
<c:set var="OTHER_PAGES" value="Other Pages"/>
<c:set var="GENES_INVOLVED" value="Genes Involved"/>
<c:set var="ZEBRAFISH_MODELS" value="Zebrafish Models"/>
<c:set var="EXPRESSION" value="Expression"/>
<c:set var="PHENOTYPE" value="Phenotype"/>

<z:dataPage sections="${[SUMMARY, RELATIONSHIP, OTHER_PAGES, GENES_INVOLVED, ZEBRAFISH_MODELS, EXPRESSION, PHENOTYPE]}">
    <jsp:attribute name="entityName">
       ${term.termName}
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/ontology/term-detail/${formBean.term.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">${term.ontology.commonName}</div>
            <h1>${term.termName}</h1>
            <jsp:include page="term-view-summary.jsp"/>
        </div>

        <z:section title="${RELATIONSHIP}" infoPopup="/action/ontology/note/ontology-relationship">
            <jsp:include page="term-view-relationship.jsp"/>
        </z:section>

        <c:if test="${isDiseaseTerm}">
            <z:section title="${OTHER_PAGES}">
                <zfin2:subsection title="${title}" test="${!empty formBean.agrDiseaseLinks}" showNoData="true" noDataText="No links to external sites">
                    <table class="horizontal-solidblock">
                        <c:forEach var="link" items="${formBean.agrDiseaseLinks}" varStatus="loop">
                            <tr>
                                <td>
                                    <zfin:link entity="${link}"/>
                                    <c:if test="${link.publicationCount > 0}">
                                        <c:choose>
                                            <c:when test="${link.publicationCount == 1}">
                                                (<a href="/${link.singlePublication.zdbID}">${link.publicationCount}</a>)
                                            </c:when>
                                            <c:otherwise>
                                                (<a href="/action/infrastructure/data-citation-list/${link.zdbID}">${link.publicationCount}</a>)
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </zfin2:subsection>
            </z:section>
            <z:section title="${ZEBRAFISH_MODELS}">
                <div class="__react-root" id="TermZebrafishModelTable"
                     data-term-id="${term.zdbID}"
                     data-direct-annotation-only="true"
                ></div>
            </z:section>
        </c:if>

        <c:if test="${formBean.term.ontology.expressionData}">
            <z:section title="${EXPRESSION}" infoPopup="/ZFIN/help_files/expression_help.html">
                <z:section title="Genes with most Figures">
                    <div class="__react-root" id="TermExpressedGenesTable"
                         data-term-id="${term.zdbID}"
                         data-direct-annotation-only="true"
                    ></div>
                </z:section>
                <z:section title="In Situ Probes">
                    <div class="__react-root" id="TermInSituProbeTable"
                         data-term-id="${term.zdbID}"
                         data-direct-annotation-only="true"
                    ></div>
                </z:section>
                <z:section title="Antibody Labeling">
                    <div class="__react-root" id="TermAntibodyTable"
                         data-term-id="${term.zdbID}"
                         data-direct-annotation-only="true"
                    ></div>
                </z:section>
            </z:section>
        </c:if>

        <c:if test="${showPhenotypeSection}">
            <z:section title="${PHENOTYPE}">
                <z:section title="Phenotype in caused by Genes">
                    <div class="__react-root" id="TermPhenotypeTable"
                         data-term-id="${term.zdbID}"
                         data-direct-annotation-only="true"
                    ></div>
                </z:section>
            </z:section>
        </c:if>

    </jsp:body>

</z:dataPage>
