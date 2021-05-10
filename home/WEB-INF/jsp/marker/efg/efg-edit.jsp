<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="NOMENCLATURE" value="Nomenclature"/>
<c:set var="NOTES" value="Notes"/>

<z:dataPage sections="${[NOMENCLATURE, RESOURCES, NOTES, MARKER_RELATIONSHIPS, SEQUENCES, ORTHOLOGY]}">
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/${efg.zdbID}">View</a>
        <a class="dropdown-item active" href="/action/marker/gene/edit/${efg.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${gene.zdbID}">Merge</a>
    </z:dataManagerDropdown>

    <h1>Edit ${efg.zdbID}</h1>

    <z:section title="${NOMENCLATURE}">
        <div class="__react-root"
             id="MarkerEditNomenclature"
             data-marker-id="${efg.zdbID}"
             data-show-abbreviation-field="false"
             data-show-reason-fields="false">
        </div>
    </z:section>

    <z:section title="${NOTES}">
        <div class="__react-root"
             id="MarkerEditNotes"
             data-current-user-id="${currentUser.zdbID}"
             data-marker-id="${efg.zdbID}">
        </div>
    </z:section>

</z:dataPage>
