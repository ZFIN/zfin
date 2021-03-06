<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="antibody" class="org.zfin.antibody.Antibody" scope="request"/>

<c:set var="DETAILS" value="Antibody Details" />
<c:set var="NOTES" value="Notes" />
<c:set var="SUPPLIERS" value="Suppliers" />

<z:dataPage sections="${[DETAILS, NOTES, SUPPLIERS]}">
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/${antibody.zdbID}">View</a>
        <a class="dropdown-item" href="/action/marker/marker-edit?zdbID=${antibody.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/infrastructure/deleteRecord/${antibody.zdbID}">Delete</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${antibody.zdbID}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/action/marker/antibody/prototype-edit/${antibody.zdbID}">Prototype Edit</a>
    </z:dataManagerDropdown>

    <h1>Edit ${antibody.zdbID}</h1>

    <z:section title="${DETAILS}">
        <div class="__react-root"
             id="AntibodyEditDetails"
             data-antibody-id="${antibody.zdbID}"
             data-host-species-list='${hostSpeciesList}'
             data-immunogen-species-list='${immunogenSpeciesList}'
             data-heavy-chain-isotypes='${heavyChainIsotypes}'
             data-light-chain-isotypes='${lightChainIsotypes}'
             data-clonal-types='${clonalTypes}'>
        </div>
    </z:section>

    <z:section title="${NOTES}">
        <div class="__react-root"
             id="MarkerEditNotes"
             data-current-user-id="${currentUser.zdbID}"
             data-marker-id="${antibody.zdbID}"
             data-show-external-notes="true">
        </div>
    </z:section>

    <z:section title="${SUPPLIERS}">
        <div class="__react-root"
             id="MarkerEditSuppliers"
             data-marker-id="${antibody.zdbID}">
        </div>
    </z:section>
</z:dataPage>