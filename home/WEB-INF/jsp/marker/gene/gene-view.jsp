<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="EXPRESSION" value="Expression"/>
<c:set var="PHENOTYPE" value="Phenotype"/>
<c:set var="ANTIBODIES" value="Antibodies"/>
<c:set var="PLASMIDS" value="Plasmids"/>
<c:set var="PATHWAYS" value="Interactions and Pathways"/>
<c:set var="MUTANTS" value="Mutations"/>
<c:set var="DISEASES" value="Human Disease"/>
<c:set var="GO" value="Gene Ontology"/>
<c:set var="PROTEINS" value="Protein Domains"/>
<c:set var="CONSTRUCTS" value="Constructs"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>
<c:set var="TRANSCRIPTS" value="Transcripts"/>
<c:set var="ORTHOLOGY" value="Orthology"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage
        sections="${[SUMMARY, EXPRESSION, PHENOTYPE, MUTANTS, DISEASES, GO, PROTEINS, TRANSCRIPTS, PATHWAYS, ANTIBODIES, PLASMIDS, CONSTRUCTS, MARKERRELATIONSHIPS, SEQUENCES, ORTHOLOGY, CITATIONS]}"
>
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item active" href="/action/marker/gene/prototype-view/${formBean.marker.zdbID}">View</a>
            <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}" />
            <jsp:include page="gene-view-summary.jsp"/>
        </div>

        <z:section title="${EXPRESSION}" infoPopup="/ZFIN/help_files/expression_help.html">
            <jsp:include page="gene-view-expression-header.jsp"/>
            <z:section title="Wild Type Expression Summary">
                <div class="__react-root" id="GeneExpressionRibbon" data-gene-id="${formBean.marker.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${PHENOTYPE}" infoPopup="/action/marker/note/phenotype">
            <jsp:include page="gene-view-phenotype-header.jsp"/>
            <z:section title="Phenotype Summary">
                <div class="__react-root" id="PhenotypeRibbon" data-gene-id="${formBean.marker.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${MUTANTS}">
            <z:section title="Mutants">
                <div class="__react-root" id="GeneAlleleTable" data-gene-id="${formBean.marker.zdbID}"></div>
            </z:section>
            <z:section title="Sequence Targeting Reagents">
                <div class="__react-root" id="GeneSTRTable" data-gene-id="${formBean.marker.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${DISEASES}">
            <z:section title="Associated With <i>${formBean.marker.abbreviation}</i> Human Ortholog" infoPopup="/action/marker/note/omim-phenotype">
                <jsp:include page="gene-view-disease-via-ortholog.jsp" />
            </z:section>
            <z:section title="Associated With <i>${formBean.marker.abbreviation}</i> Via Experimental Models" infoPopup="/action/marker/note/disease-model">
                <jsp:include page="gene-view-disease-via-experiment.jsp" />
            </z:section>
        </z:section>

        <z:section title="${GO}">
            <div class="__react-root" id="GeneOntologyRibbon" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${PROTEINS}">
            <z:section title="Domain, Family, and Site Summary">
                <jsp:include page="gene-view-proteins.jsp"/>
            </z:section>
            <z:section title="Domain Details Per Protein">
                <jsp:include page="gene-view-protein-detail.jsp"/>
            </z:section>
        </z:section>

        <z:section title="${TRANSCRIPTS}">
            <jsp:include page="gene-view-transcripts.jsp"/>
        </z:section>

        <z:section title="${PATHWAYS}">
            <jsp:include page="gene-view-pathways.jsp"/>
        </z:section>

        <z:section title="${ANTIBODIES}">
            <jsp:include page="gene-view-antibodies.jsp"/>
        </z:section>

        <z:section title="${PLASMIDS}">
            <jsp:include page="gene-view-plasmids.jsp"/>
        </z:section>

        <z:section title="${CONSTRUCTS}">
            <div class="__react-root" id="GeneConstructsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${MARKERRELATIONSHIPS}">
            <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>



        <z:section title="${SEQUENCES}">
            <div
                class="__react-root"
                id="MarkerSequencesTable"
                data-marker-id="${formBean.marker.zdbID}"
                data-show-summary="true"
            >
            </div>
        </z:section>

        <z:section title="${ORTHOLOGY}">
            <jsp:include page="gene-view-orthology.jsp"/>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="MarkerCitationsTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>

</z:dataPage>
