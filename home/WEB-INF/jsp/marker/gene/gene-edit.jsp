<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="NOMENCLATURE" value="Nomenclature"/>
<c:set var="RESOURCES" value="Genome Resources"/>
<c:set var="NOTES" value="Notes"/>
<c:set var="MARKER_RELATIONSHIPS" value="Marker Relationships"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="ORTHOLOGY" value="Orthology"/>
<c:set var="CHROMOSOME_INFORMATION" value="Chromosome Information"/>

<z:dataPage sections="${[NOMENCLATURE, RESOURCES, NOTES, MARKER_RELATIONSHIPS, SEQUENCES, ORTHOLOGY]}">
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/${gene.zdbID}">View</a>
        <a class="dropdown-item active" href="/action/marker/gene/edit/${gene.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${gene.zdbID}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/action/marker/gene/prototype-edit/${gene.zdbID}">Prototype Edit</a>
    </z:dataManagerDropdown>

    <h1>Edit ${gene.zdbID}</h1>

    <z:section title="${NOMENCLATURE}">
        <div class="__react-root" id="MarkerEditNomenclature" data-marker-id="${gene.zdbID}"></div>
    </z:section>

    <z:section title="${RESOURCES}">
        <div class="__react-root" id="MarkerEditDbLinks" data-marker-id="${gene.zdbID}"></div>
    </z:section>

    <z:section title="${NOTES}">
        <div class="__react-root"
             id="MarkerEditNotes"
             data-current-user-id="${currentUser.zdbID}"
             data-marker-id="${gene.zdbID}">
        </div>
    </z:section>

    <z:section title="${MARKER_RELATIONSHIPS}">
        <div class="__react-root"
             id="MarkerEditMarkerRelationships"
             data-marker-abbreviation="${gene.abbreviation}"
             data-marker-id="${gene.zdbID}"
             data-relationship-type-data='${markerRelationshipTypes}'>
        </div>
    </z:section>

    <z:section title="${CHROMOSOME_INFORMATION}">
        <div class="__react-root"
             id="MarkerEditChromosomeInformation"
             data-type="Chromosome Information"
             data-marker-id="${gene.zdbID}"
             ></div>
    </z:section>

    <z:section title="${SEQUENCES}">
        <z:section title="Sequences">
            <div class="__react-root" id="MarkerEditSequences" data-marker-id="${gene.zdbID}"></div>
        </z:section>
        <z:section title="Nucleotide Sequences">
            <div class="__react-root" id="MarkerAddSequences" data-marker-id="${gene.zdbID}"
                 data-type="Nucleotide"></div>
        </z:section>
        <c:if test="${typeName ne 'MIRNAG'}">
            <z:section title="Protein Sequences">
                <div class="__react-root" id="MarkerAddSequences" data-marker-id="${gene.zdbID}"
                     data-type="Protein"></div>
            </z:section>
        </c:if>
    </z:section>


    <z:section title="${ORTHOLOGY}">
        <div class="__react-root"
             id="MarkerEditOrthology"
             data-marker-id="${gene.zdbID}">
        </div>
    </z:section>
</z:dataPage>
