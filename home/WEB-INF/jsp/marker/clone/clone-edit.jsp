<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="NOMENCLATURE" value="Nomenclature" />

<z:dataPage sections="${[NOMENCLATURE]}">
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
</z:dataPage>