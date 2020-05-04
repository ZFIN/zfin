<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="RELATEDTRANSCRIPTS" value="Related Transcripts"/>
<c:set var="SEQUENCE" value="Sequence"/>
<c:set var="GBROWSE" value="Gbrowse"/>
<c:set var="SEGMENTRELATIONSHIPS" value="Segment Relationships"/>
<c:set var="PROTEINS" value="Protein Products"/>
<c:set var="SUPPORTINGSEQUENCES" value="Supporting Sequences"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage
        sections="${[SUMMARY, RELATEDTRANSCRIPTS, SEQUENCE, GBROWSE, SEGMENTRELATIONSHIPS, PROTEINS, SUPPORTINGSEQUENCES, CITATIONS]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item active"
               href="/action/marker/transcript/prototype-view/${formBean.marker.zdbID}">View</a>
            <div class="dropdown-divider"></div>

            <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <jsp:include page="transcript-view-summary.jsp"/>
        </div>

        <z:section title="${RELATEDTRANSCRIPTS}">
            <z:section title="Confirmed Transcripts">
                <jsp:include page="transcript-view-related-transcripts.jsp"/>
            </z:section>
            <authz:authorize access="hasRole('root')">
                <z:section title="Withdrawn Transcripts">
                    <jsp:include page="transcript-view-withdrawn-transcripts.jsp"/>
                </z:section>
            </authz:authorize>
        </z:section>

        <z:section title="${SEQUENCE}">
            <jsp:include page="transcript-view-sequence.jsp"/>
        </z:section>

        <z:section title="${GBROWSE}">
            <jsp:include page="transcript-view-gbrowse.jsp"/>
        </z:section>

        <z:section title="${SEGMENTRELATIONSHIPS}">
            <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${PROTEINS}">
            <jsp:include page="transcript-view-protein-products.jsp"/>
        </z:section>

        <z:section title="${SUPPORTINGSEQUENCES}">
            <div class="__react-root" id="GeneSequencesTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="MarkerCitationsTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>
