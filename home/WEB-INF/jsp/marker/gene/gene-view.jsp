<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ANTIBODIES" value="Antibodies"/>
<c:set var="PLASMIDS" value="Plasmids"/>
<c:set var="PATHWAYS" value="Interactions and Pathways"/>
<c:set var="MUTANTS" value="Mutations"/>
<c:set var="DISEASES" value="Disease"/>
<c:set var="GO" value="Gene Ontology"/>
<c:set var="PROTEINS" value="Proteins"/>
<c:set var="CONSTRUCTS" value="Constructs"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>
<c:set var="TRANSCRIPTS" value="Transcripts"/>
<c:set var="ORTHOLOGY" value="Orthology"/>

<zfin-prototype:dataPage
        sections="${[SUMMARY, MUTANTS, DISEASES, GO, PROTEINS, PATHWAYS, ANTIBODIES, PLASMIDS, CONSTRUCTS, MARKERRELATIONSHIPS, TRANSCRIPTS, SEQUENCES, ORTHOLOGY]}"
>
    <zfin-prototype:dataManagerDropdown>
        <a class="dropdown-item active" href="/action/marker/gene/prototype-view/${formBean.marker.zdbID}">View</a>
        <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
    </zfin-prototype:dataManagerDropdown>

    <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
    <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

    <div id="${zfn:makeDomIdentifier(SUMMARY)}">
        <jsp:include page="gene-view-summary.jsp" />
    </div>

    <zfin-prototype:section title="${MUTANTS}">
        <zfin-prototype:section title="Mutants">
            <jsp:include page="gene-view-mutants.jsp" />
        </zfin-prototype:section>
        <zfin-prototype:section title="Sequence Targeting Reagents">
            <jsp:include page="gene-view-strs.jsp" />
        </zfin-prototype:section>
    </zfin-prototype:section>

    <zfin-prototype:section title="${DISEASES}">
        <zfin-prototype:section title="Associated with <i>${formBean.marker.abbreviation}</i> human ortholog">
            <jsp:include page="gene-view-disease-via-ortholog.jsp" />
        </zfin-prototype:section>
        <zfin-prototype:section title="Associated with <i>${formBean.marker.abbreviation}</i> via experimental Models">
            <jsp:include page="gene-view-disease-via-experiment.jsp" />
        </zfin-prototype:section>
    </zfin-prototype:section>

    <zfin-prototype:section title="${GO}">
        <div class="__react-root" id="GeneOntologyRibbon" data-gene-id="${formBean.marker.zdbID}"></div>
    </zfin-prototype:section>

    <zfin-prototype:section title="${PROTEINS}">
        <jsp:include page="gene-view-proteins.jsp" />
    </zfin-prototype:section>

    <zfin-prototype:section title="${PATHWAYS}">
        <jsp:include page="gene-view-pathways.jsp" />
    </zfin-prototype:section>

    <zfin-prototype:section title="${ANTIBODIES}">
        <jsp:include page="gene-view-antibodies.jsp" />
    </zfin-prototype:section>

    <zfin-prototype:section title="${PLASMIDS}">
        <jsp:include page="gene-view-plasmids.jsp" />
    </zfin-prototype:section>

    <zfin-prototype:section title="${TRANSCRIPTS}">
        <zfin2:markerTranscriptSummary
                relatedTranscriptDisplay="${formBean.relatedTranscriptDisplay}"
                locations="${formBean.locations}"
                showAllTranscripts="true"
        />
    </zfin-prototype:section>

    <zfin-prototype:section title="${CONSTRUCTS}">
        <div class="__react-root" id="GeneConstructsTable" data-gene-id="${formBean.marker.zdbID}"></div>
    </zfin-prototype:section>

    <zfin-prototype:section title="${MARKERRELATIONSHIPS}">
        <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
    </zfin-prototype:section>

    <zfin-prototype:section title="${SEQUENCES}">
        <div class="__react-root" id="GeneSequencesTable" data-gene-id="${formBean.marker.zdbID}"></div>
    </zfin-prototype:section>

    <zfin-prototype:section title="${ORTHOLOGY}">
        <jsp:include page="gene-view-orthology.jsp" />
    </zfin-prototype:section>
</zfin-prototype:dataPage>

<script src="${zfn:getAssetPath("react.js")}"></script>
