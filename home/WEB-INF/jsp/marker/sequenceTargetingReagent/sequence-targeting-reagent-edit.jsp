<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="str" class="org.zfin.mutant.SequenceTargetingReagent" scope="request"/>

<c:set var="NOMENCLATURE" value="Nomenclature" />
<c:set var="TARGETS" value="Targets" />

<z:dataPage sections="${[NOMENCLATURE, TARGETS]}">
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/${str.zdbID}">View</a>
        <a class="dropdown-item" href="/action/str/${str.zdbID}/edit">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${str.zdbID}">Merge</a>
        <a class="dropdown-item" href="/action/infrastructure/deleteRecord/${str.zdbID}">Delete</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/action/marker/str/prototype-edit/${str.zdbID}">Prototype Edit</a>
    </z:dataManagerDropdown>

    <h1>Edit ${str.zdbID}</h1>

    <z:section title="${NOMENCLATURE}">
        <div class="__react-root"
             id="MarkerEditNomenclature"
             data-marker-id="${str.zdbID}"
             data-show-abbreviation-field="false"
             data-show-reason-fields="false">
        </div>
    </z:section>

    <z:section title="${TARGETS}">
        <div class="__react-root"
             id="MarkerEditMarkerRelationships"
             data-marker-abbreviation="${str.abbreviation}"
             data-marker-id="${str.zdbID}"
             data-show-relationship-type="false"
             data-relationship-type-data='${markerRelationshipTypes}'>
        </div>
    </z:section>
</z:dataPage>