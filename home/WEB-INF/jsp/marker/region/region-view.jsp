<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="MUTATIONS" value="Mutations and Sequence Targeting Reagent"/>
<c:set var="GO" value="Gene Ontology"/>
<c:set var="TRANSCRIPTS" value="Transcripts"/>
<c:set var="CONSTRUCTS" value="Constructs with Sequences"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>
<c:set var="PATHWAYS" value="Interactions and Pathways"/>
<c:set var="SEQUENCE" value="Sequence Information"/>
<c:set var="ORTHOLOGY" value="Orthology"/>

<z:dataPage sections="${[SUMMARY, MUTATIONS, GO, TRANSCRIPTS, CONSTRUCTS, PATHWAYS, SEQUENCE, ORTHOLOGY]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/marker/gene/edit/">Edit</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=">Merge</a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <jsp:include page="region-view-summary.jsp"/>
        </div>

        <z:section title="${MUTATIONS}">
            <z:section title="Mutations">
                <jsp:include page="region-view-mutations.jsp"/>
            </z:section>
            <z:section title="Sequence Targeting Reagent">
                <jsp:include page="region-view-str.jsp"/>
            </z:section>
        </z:section>

        <z:section title="${GO}">
            <div class="__react-root" id="GeneOntologyRibbon" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${TRANSCRIPTS}">
            <jsp:include page="../gene/gene-view-transcripts.jsp"/>
        </z:section>

        <z:section title="${CONSTRUCTS}">
            <div class="__react-root" id="GeneConstructsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${PATHWAYS}">
            <!-- TODo -->
        </z:section>

        <z:section title="${MARKERRELATIONSHIPS}">
            <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${SEQUENCE}">
            <div class="__react-root" id="GeneSequencesTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${ORTHOLOGY}">
            <jsp:include page="../gene/gene-view-orthology.jsp"/>
        </z:section>
    </jsp:body>
</z:dataPage>

<script src="${zfn:getAssetPath("react.js")}"></script>
