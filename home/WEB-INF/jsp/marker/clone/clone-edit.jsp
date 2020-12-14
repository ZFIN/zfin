<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="NOMENCLATURE" value="Nomenclature" />
<c:set var="NOTES" value="Notes" />
<c:set var="MARKER_RELATIONSHIPS" value="Marker Relationships" />
<c:set var="SEQUENCES" value="Sequences" />

<z:dataPage sections="${[NOMENCLATURE, NOTES, MARKER_RELATIONSHIPS, SEQUENCES]}">
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/${clone.zdbID}">View</a>
        <a class="dropdown-item" href="/action/marker/marker-edit?zdbID=${clone.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${clone.zdbID}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/action/marker/clone/prototype-edit/${clone.zdbID}">Prototype Edit</a>
    </z:dataManagerDropdown>

    <h1>Edit ${clone.zdbID}</h1>

    <z:section title="${NOMENCLATURE}">
        <div class="__react-root"
             id="MarkerEditNomenclature"
             data-marker-id="${clone.zdbID}"
             data-show-abbreviation-field="false"
             data-show-reason-fields="false">
        </div>
    </z:section>

    <z:section title="${NOTES}">
        <div class="__react-root"
             id="MarkerEditNotes"
             data-current-user-id="${currentUser.zdbID}"
             data-marker-id="${clone.zdbID}">
        </div>
    </z:section>

    <z:section title="${MARKER_RELATIONSHIPS}">
        <div class="__react-root"
             id="MarkerEditMarkerRelationships"
             data-marker-abbreviation="${clone.abbreviation}"
             data-marker-id="${clone.zdbID}"
             data-relationship-type-data='${markerRelationshipTypes}'>
        </div>
    </z:section>

    <z:section title="${SEQUENCES}">
        <div class="__react-root"
             id="MarkerEditSequences"
             data-group="dblink adding on clone-edit"
             data-group-d-b="dblink adding on clone-edit"
             data-marker-id="${clone.zdbID}">
        </div>
    </z:section>
</z:dataPage>