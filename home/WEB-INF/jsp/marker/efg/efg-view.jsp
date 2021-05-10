<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="CONSTRUCTS" value="Constructs"/>
<c:set var="ANTIBODIES" value="Antibodies"/>
<c:set var="GENOTYPE" value="Expression"/>
<c:set var="SEQUENCE" value="Sequence Information"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, GENOTYPE, CONSTRUCTS, ANTIBODIES, SEQUENCE, CITATIONS]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
            <a class="dropdown-item" href="/action/infrastructure/deleteRecord/${formBean.marker.zdbID}">Delete</a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" href="/action/marker/efg/prototype-edit/${formBean.marker.zdbID}">Prototype Edit</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}" />
            <jsp:include page="efg-view-summary.jsp"/>
        </div>

        <z:section title="${GENOTYPE}">
            <jsp:include page="efg-view-expression-header.jsp"/>
        </z:section>

        <z:section title="${CONSTRUCTS}">
            <div class="__react-root" id="GeneConstructsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${ANTIBODIES}" infoPopup="/action/marker/note/antibodies">
            <jsp:include page="../gene/gene-view-antibodies.jsp"/>
        </z:section>

        <z:section title="${SEQUENCE}">
            <div
                class="__react-root"
                id="MarkerSequencesTable"
                data-marker-id="${formBean.marker.zdbID}"
                data-show-summary="true"
            >
            </div>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>

