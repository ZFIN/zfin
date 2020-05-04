<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.AntibodyMarkerBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="NOTES" value="Notes"/>
<c:set var="LABELING" value="Anatomical Labeling"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, NOTES, LABELING, CITATIONS]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.editURL}">Edit</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.deleteURL}">Delete</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.mergeURL}">Merge</a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <jsp:include page="antibody-view-summary.jsp"/>
        </div>

        <z:section title="${NOTES}">
            <jsp:include page="antibody-view-notes.jsp"/>
        </z:section>

        <z:section title="${LABELING}">
            <jsp:include page="antibody-view-labeling.jsp"/>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="MarkerCitationsTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>
