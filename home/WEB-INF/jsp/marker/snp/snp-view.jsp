<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SnpMarkerBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, MARKERRELATIONSHIPS, CITATIONS]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}" />
            <jsp:include page="snp-view-summary.jsp"/>
        </div>

        <z:section title="${MARKERRELATIONSHIPS}">
            <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>