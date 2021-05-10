<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="NOMENCLATURE" value="Nomenclature"/>
<c:set var="NOTES" value="Notes"/>

<z:dataPage sections="${[NOMENCLATURE, RESOURCES, NOTES, MARKER_RELATIONSHIPS, SEQUENCES, ORTHOLOGY]}">
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/${eregion.zdbID}">View</a>
        <a class="dropdown-item active" href="/action/marker/gene/edit/${eregion.zdbID}">Edit</a>
    </z:dataManagerDropdown>

    <h1>Edit ${eregion.zdbID}</h1>

    <z:section title="${NOMENCLATURE}">
        <div class="__react-root"
             id="MarkerEditNomenclature"
             data-marker-id="${eregion.zdbID}"
             data-show-abbreviation-field="false"
             data-show-reason-fields="false">
        </div>
    </z:section>

    <z:section title="${NOTES}">
        <div class="__react-root"
             id="MarkerEditNotes"
             data-current-user-id="${currentUser.zdbID}"
             data-marker-id="${eregion.zdbID}">
        </div>
    </z:section>

</z:dataPage>
