<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>
<c:set var="term" value="${formBean.term}"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="RELATIONSHIP" value="Relationships"/>
<c:set var="EXPRESSION" value="Expression"/>
<c:set var="PHENOTYPE" value="Phenotype"/>

<z:dataPage sections="${[SUMMARY, RELATIONSHIP, EXPRESSION, PHENOTYPE]}">
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

        <z:section title="${PHENOTYPE}">
            <z:section title="Phenotype in caused by Genes">
                <div class="__react-root" id="TermPhenotypeTable"
                     data-term-id="${term.zdbID}"
                     data-direct-annotation-only="true"
                ></div>
            </z:section>
        </z:section>

    </jsp:body>

</z:dataPage>
