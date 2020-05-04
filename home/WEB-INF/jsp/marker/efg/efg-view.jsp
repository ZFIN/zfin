<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="CONSTRUCTS" value="Constructs"/>
<c:set var="EXPRESSION" value="Expression"/>
<c:set var="SEQUENCE" value="Sequence Information"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, EXPRESSION, CONSTRUCTS, SEQUENCE, CITATIONS]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <jsp:include page="efg-view-summary.jsp"/>
        </div>

        <z:section title="${EXPRESSION}">
            <jsp:include page="efg-view-expression-header.jsp"/>
        </z:section>

        <z:section title="${CONSTRUCTS}">
            <div class="__react-root" id="GeneConstructsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${SEQUENCE}">
            <div class="__react-root" id="GeneSequencesTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="MarkerCitationsTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>

