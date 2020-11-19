<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="NOMENCLATURE" value="Nomenclature" />
<c:set var="RESOURCES" value="Genome Resources" />
<c:set var="NOTES" value="Notes" />
<c:set var="MARKER_RELATIONSHIPS" value="Marker Relationships" />

<z:dataPage sections="${[NOMENCLATURE, RESOURCES, NOTES, MARKER_RELATIONSHIPS]}">
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
        <div class="__react-root" id="MarkerEditGenomeResources" data-marker-id="${gene.zdbID}"></div>
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
             data-marker-id="${gene.zdbID}"
             data-relationship-types="${markerRelationshipTypes}">
        </div>
    </z:section>
</z:dataPage>
