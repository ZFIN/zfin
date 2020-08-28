<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="NOMENCLATURE" value="Nomenclature" />

<z:dataPage sections="${[NOMENCLATURE]}">
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
</z:dataPage>
