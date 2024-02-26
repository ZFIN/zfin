<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.framework.presentation.NavigationMenuOptions" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<c:set var="SUMMARY" value="${NavigationMenuOptions.SUMMARY.value}"/>
<c:set var="SEQUENCE" value="${NavigationMenuOptions.SEQUENCE.value}"/>
<c:set var="RELATEDTRANSCRIPTS" value="${NavigationMenuOptions.RELATED_TRANSCRIPTS.value}"/>
<c:set var="SEGMENTRELATIONSHIPS" value="${NavigationMenuOptions.SEGEMENT_RELATIONSHIPS.value}"/>
<c:set var="PROTEINS" value="${NavigationMenuOptions.PROTEIN_PRODUCTS.value}"/>
<c:set var="SUPPORTINGSEQUENCES" value="${NavigationMenuOptions.SUPPORTING_SEQUENCE.value}"/>
<c:set var="DB_LINKS" value="${NavigationMenuOptions.DB_LINKS.value}"/>
<c:set var="CITATIONS" value="${NavigationMenuOptions.CITATION.value}"/>


<%--
<c:set var="TARGETGENES" value="Target Genes"/>
<c:set var="DB_LINKS" value="DB_LINK Records"/>
--%>
<c:set var="BODYCLASSES" value="publication-view nav-title-wrap-break-word"/>

<c:if test="${formBean.marker.transcriptType.display eq 'miRNA'}">
    <c:set var="sections" value="${[SUMMARY, TARGETGENES, SEQUENCE, RELATEDTRANSCRIPTS, SEGMENTRELATIONSHIPS, PROTEINS, SUPPORTINGSEQUENCES, CITATIONS]}"/>
</c:if>
<c:if test="${formBean.marker.transcriptType.display ne 'miRNA'}">
    <c:set var="sections" value="${[SUMMARY, SEQUENCE, RELATEDTRANSCRIPTS, SEGMENTRELATIONSHIPS, PROTEINS, SUPPORTINGSEQUENCES, CITATIONS]}"/>
</c:if>

<z:dataPage sections="${[]}" navigationMenu="${navigationMenu}" additionalBodyClass="${BODYCLASSES}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>

        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/marker/transcript/edit/${formBean.marker.zdbID}">Edit</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
            <a class="dropdown-item" href="/action/infrastructure/deleteRecord/${formBean.marker.zdbID}">Delete</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}"/>
            <jsp:include page="transcript-view-summary.jsp"/>
        </div>

        <c:if test="${formBean.marker.transcriptType.display eq 'miRNA'}">
            <z:section title="${TARGETGENES}">
                <jsp:include page="transcript-view-targets.jsp"/>
            </z:section>
        </c:if>

        <z:section title="${SEQUENCE}">
            <jsp:include page="transcript-view-sequence.jsp"/>
        </z:section>

        <z:section title="${RELATEDTRANSCRIPTS}">
            <jsp:include page="transcript-view-related-transcripts.jsp"/>
        </z:section>

        <z:section title="${SEGMENTRELATIONSHIPS}">
            <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${PROTEINS}">
            <jsp:include page="transcript-view-protein-products.jsp"/>
        </z:section>

        <z:section title="${SUPPORTINGSEQUENCES}">
            <div
                    class="__react-root"
                    id="MarkerSequencesTable"
                    data-marker-id="${formBean.marker.zdbID}"
                    data-show-summary="true"
            >
            </div>
        </z:section>
        <authz:authorize access="hasRole('root')">
            <z:section title="${DB_LINKS}">
                <div
                        class="__react-root"
                        id="TranscriptDbLinkTable"
                        data-marker-id="${formBean.marker.zdbID}"
                        data-show-summary="true"
                >
                </div>
            </z:section>
        </authz:authorize>
        <z:section title="${CITATIONS}">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>
