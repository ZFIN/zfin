<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.framework.presentation.NavigationMenuOptions" %>
<%@ page import="org.zfin.framework.featureflag.FeatureFlagEnum" %>
<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.indexer.UiIndexerConfig" %>
<%@ page import="org.zfin.framework.presentation.LookupStrings" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>
<c:set var="term" value="${formBean.term}"/>

<c:set var="SUMMARY" value="${NavigationMenuOptions.SUMMARY.value}"/>
<c:set var="RELATIONSHIP" value="${NavigationMenuOptions.RELATIONSHIPS.value}"/>
<c:set var="HUMAN_DISEASE" value="${NavigationMenuOptions.CHEBI_HUMAN_DISEASE.value}"/>
<c:set var="PHENOTYPE" value="${NavigationMenuOptions.PHENOTYPE.value}"/>
<c:set var="ALLELES" value="${NavigationMenuOptions.ALLELE.value}"/>
<c:set var="PHENOTYPE_CHEBI" value="${NavigationMenuOptions.PHENOTYPE_CHEBI.value}"/>
<c:set var="GENES_INVOLVED" value="${NavigationMenuOptions.GENES_INVOLVED.value}"/>
<c:set var="ZEBRAFISH_MODELS" value="${NavigationMenuOptions.ZEBRAFISH_MODELS.value}"/>
<c:set var="EXPRESSION" value="${NavigationMenuOptions.EXPRESSION.value}"/>
<c:set var="OTHER_PAGES" value="${NavigationMenuOptions.OTHER_PAGE.value}"/>

<c:set var="secs"/>

<z:dataPage sections="${[]}" navigationMenu="${navigationMenu}" additionalBodyClass="term-view nav-title-wrap-break-word">

    <jsp:attribute name="entityName">
       ${term.termName}
    </jsp:attribute>

    <jsp:body>
        <div class="float-right">
            Search Ontology: <zfin2:lookup ontologyName="${Ontology.AOGODOCHEBI.toString()}"
                                           action="${LookupStrings.ACTION_TERM_SEARCH}"
                                           wildcard="true" useIdAsTerm="true" termsWithDataOnly="false"/>
        </div>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">${term.ontology.commonName}</div>
            <h1>${term.termName}</h1>
            <jsp:include page="term-view-summary.jsp"/>
        </div>

        <z:section title="${RELATIONSHIP}" infoPopup="/action/ontology/note/ontology-relationship">
            <jsp:include page="term-view-relationship.jsp"/>
        </z:section>

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

        <z:section title="${GENES_INVOLVED}">
            <div class="__react-root" id="TermGeneTable"
                 data-term-id="${term.zdbID}"
                 data-direct-annotation-only="true"
            ></div>
        </z:section>
        <z:section title="${ZEBRAFISH_MODELS}">
            <div class="__react-root" id="TermZebrafishModelTable"
                 data-term-id="${term.zdbID}"
                 data-direct-annotation-only="true"
            ></div>
        </z:section>
        <z:section title="${PHENOTYPE_CHEBI}">
            <z:section title="Phenotype resulting from" appendedText="${term.termName}" show="${true}">
                <div class="__react-root" id="ChebiPhenotypeTable"
                     data-term-id="${term.zdbID}"
                     data-direct-annotation-only="${true}"
                     data-is-wildtype="${true}"
                     data-is-multi-chebi-condition="${false}"
                     data-show-dev-info="${showDevInfo}"
                             data-indexer=${UiIndexerConfig.ChebiPhenotypeIndexer.typeName}
                ></div>
            </z:section>
            <z:section title="Phenotype where environments contain" appendedText="${term.termName}" show="${true}">
                <div class="__react-root" id="ChebiPhenotypeTable"
                     data-term-id="${term.zdbID}"
                     data-direct-annotation-only="true"
                     data-show-dev-info=${showDevInfo}
                             data-indexer=${UiIndexerConfig.ChebiPhenotypeIndexer.typeName}
                ></div>
            </z:section>
            <z:section title="Phenotype modified by environments containing" appendedText="${term.termName}" show="${true}">
                <div class="__react-root" id="ChebiModifiedPhenotypeTable"
                     data-term-id="${term.zdbID}"
                     data-direct-annotation-only="true"
                     data-show-dev-info=${showDevInfo}
                             data-indexer=${UiIndexerConfig.ChebiPhenotypeIndexer.typeName}
                ></div>
            </z:section>
            <z:section title="Phenotype affecting" appendedText="${term.termName}" show="${true}">
                <div class="__react-root" id="ChebiPhenotypeTable"
                     data-term-id="${term.zdbID}"
                     data-direct-annotation-only="true"
                     data-has-chebi-in-phenotype="true"
                     data-show-dev-info="${showDevInfo}"
                     data-indexer=${UiIndexerConfig.ChebiPhenotypeIndexer.typeName}
                ></div>
            </z:section>
        </z:section>

        <z:section title="${HUMAN_DISEASE}">
            <div class="__react-root" id="ChebiTermZebrafishModelTable"
                 data-term-id="${term.zdbID}"
                 data-direct-annotation-only="true"
                 data-is-chebi="{true}"
            ></div>
        </z:section>

        <c:if test="${formBean.term.ontology.expressionData}">
            <z:section title="${EXPRESSION}" infoPopup="/ZFIN/help_files/expression_help.html">
                <z:section title="Genes with most Figures" show="${true}">
                    <div class="__react-root" id="TermExpressedGenesTable"
                         data-term-id="${term.zdbID}"
                         data-direct-annotation-only="true"
                    ></div>
                    <div><a href="/action/expression/results?anatomyTermNames=${term.termName}&anatomyTermIDs=${term.zdbID}&journalType=ALL&includeSubstructures=false&onlyWildtype=true">
                        Search genes within Advanced Search</a></div>
                </z:section>
                <z:section title="Thisse recommended In Situ Probes" infoPopup="/action/ontology/clone-stars" show="${true}">
                    <div class="__react-root" id="TermInSituProbeTable"
                         data-term-id="${term.zdbID}"
                         data-direct-annotation-only="true"
                    ></div>
                </z:section>
                <z:section title="Antibody Labeling" show="${true}">
                    <div class="__react-root" id="TermAntibodyTable"
                         data-term-id="${term.zdbID}"
                         data-direct-annotation-only="true"
                    ></div>
                </z:section>
            </z:section>
        </c:if>

        <c:if test="${showPhenotypeSection}">
            <z:section title="${PHENOTYPE}">
                <z:section title="Phenotype caused by Genes" show="${true}">
                    <div class="__react-root" id="TermPhenotypeTable"
                         data-term-id="${term.zdbID}"
                         data-direct-annotation-only="true"
                    ></div>
                </z:section>
            </z:section>
        </c:if>

        <c:if test="${zfn:isFlagEnabled(FeatureFlagEnum.SHOW_ALLIANCE_DATA)}">
            <z:section title="${ALLELES}">
                <div class="__react-root" id="TermAlleleTable"
                     data-term-id="${term.oboID}"
                ></div>
            </z:section>
        </c:if>
    </jsp:body>


</z:dataPage>
