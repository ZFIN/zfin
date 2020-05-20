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

<z:dataPage
        sections="${[SUMMARY, EXPRESSION, MUTANTS,  MARKERRELATIONSHIPS, TRANSCRIPTS, SEQUENCES, ORTHOLOGY, CITATIONS]}"
>
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item active" href="/action/marker/pseudogene/prototype-view/${formBean.marker.zdbID}">View</a>
            <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <jsp:include page="pseudogene-view-summary.jsp"/>
        </div>

        <z:section title="${EXPRESSION}" infoPopup="/ZFIN/help_files/expression_help.html">
            <jsp:include page="pseudogene-view-expression-header.jsp"/>
            <z:section title="Wild Type Expression Summary">
                <div class="__react-root" id="GeneExpressionRibbon" data-gene-id="${formBean.marker.zdbID}"></div>
            </z:section>
        </z:section>
        

        <z:section title="${MUTANTS}">
            <z:section title="Mutants">
                <jsp:include page="pseudogene-view-mutants.jsp"/>
            </z:section>
            <z:section title="Sequence Targeting Reagents">
                <jsp:include page="pseudogene-view-strs.jsp"/>
            </z:section>
        </z:section>

       
        <z:section title="${MARKERRELATIONSHIPS}">
            <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${TRANSCRIPTS}">
            <z:section title="Confirmed Transcripts">
                <jsp:include page="pseudogene-view-transcripts.jsp"/>
            </z:section>
            <authz:authorize access="hasRole('root')">
                <z:section title="Withdrawn Transcripts">
                    <jsp:include page="pseudogene-view-withdrawn-transcripts.jsp"/>
                </z:section>
            </authz:authorize>
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
            <jsp:include page="pseudogene-view-orthology.jsp"/>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="MarkerCitationsTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>

</z:dataPage>
