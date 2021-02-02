<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="MUTATIONS" value="Mutations and Sequence Targeting Reagent"/>
<c:set var="GO" value="Gene Ontology"/>
<c:set var="CONSTRUCTS" value="Constructs with Sequences"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>
<c:set var="PATHWAYS" value="Interactions and Pathways"/>
<c:set var="SEQUENCE" value="Sequence Information"/>
<c:set var="ORTHOLOGY" value="Orthology"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, MUTATIONS, GO, CONSTRUCTS, PATHWAYS, SEQUENCE, ORTHOLOGY, CITATIONS]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" href="/action/marker/gene/prototype-edit/${formBean.marker.zdbID}">Prototype Edit</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}" />
            <jsp:include page="region-view-summary.jsp"/>
        </div>

        <z:section title="${MUTATIONS}">
            <z:section title="Mutants">
                <div class="__react-root" id="GeneAlleleTable" data-gene-id="${formBean.marker.zdbID}"></div>
            </z:section>
            <z:section title="Sequence Targeting Reagents">
                <div class="__react-root" id="GeneSTRTable" data-gene-id="${formBean.marker.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${GO}">
            <div class="__react-root" id="GeneOntologyRibbon" data-gene-id="${formBean.marker.zdbID}"></div>
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
            <div
                class="__react-root"
                id="MarkerSequencesTable"
                data-marker-id="${formBean.marker.zdbID}"
            >
            </div>
        </z:section>

        <z:section title="${ORTHOLOGY}">
            <jsp:include page="../region/region-view-orthology.jsp"/>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>
