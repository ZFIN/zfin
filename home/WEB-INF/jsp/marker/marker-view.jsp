<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="CITATIONS" value="Citations"/>

<c:set var="sections" value="${[SUMMARY, MARKERRELATIONSHIPS, SEQUENCES, CITATIONS]}"/>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>

<z:dataPage sections="${sections}">

    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
<%-- Need to implement a DeleteRuleClass for STS
            <a class="dropdown-item" href="${deleteURL}">Delete</a>
--%>
            <a class="dropdown-item" href="/action/marker/marker-edit?zdbID=${formBean.marker.zdbID}">Edit</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}"/>
            <jsp:include page="generic-marker-view-summary.jsp"/>
        </div>

        <z:section title="${MARKERRELATIONSHIPS}">
            <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${SEQUENCES}">
            <div
                    class="__react-root"
                    id="MarkerSequencesTable"
                    data-marker-id="${formBean.marker.zdbID}"
            >
            </div>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="MarkerCitationsTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>



