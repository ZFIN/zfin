<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="test" value="Reference Picker Test" />

<z:dataPage sections="${[test]}">
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/action/marker/gene/prototype-view/${gene.zdbID}">View</a>
        <a class="dropdown-item active" href="/action/marker/gene/prototype-edit/${gene.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${gene.zdbID}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/${gene.zdbID}">Old View</a>
        <a class="dropdown-item" href="/action/marker/gene/edit/${gene.zdbID}">Old Edit</a>
    </z:dataManagerDropdown>

    <h1>Edit ${gene.zdbID}</h1>

    <z:section title="${test}">
        <div class="__react-root" id="MarkerEditTest" data-ortho-pubs='${orthoPubsJSON}'></div>
    </z:section>
</z:dataPage>
