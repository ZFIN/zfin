<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<c:forEach var="relatedTranscriptDisplay" items="${formBean.relatedTranscriptDisplayList}" varStatus="loop">

    <zfin-gbrowse:genomeBrowserImageComponent image="${relatedTranscriptDisplay.gbrowseImage}" loopIndex="${loop.index}" />

    <z:section show="true">
        <jsp:attribute name="title">
            Transcripts related to <zfin:abbrev entity="${relatedTranscriptDisplay.gene}" />
        </jsp:attribute>
        <jsp:body>
            <z:ifHasData test="${fn:length(relatedTranscriptDisplay.nonWithdrawnTranscripts) > 1}">
                <jsp:attribute name="noDataMessage">
                    No other transcripts related to <zfin:abbrev entity="${relatedTranscriptDisplay.gene}" />
                </jsp:attribute>
                <jsp:body>
                    <zfin2:markerTranscriptDataTable
                            transcripts="${relatedTranscriptDisplay.nonWithdrawnTranscripts}"
                            unlinkedTranscript="${formBean.marker}"
                    />
                </jsp:body>
            </z:ifHasData>
        </jsp:body>
    </z:section>

    <authz:authorize access="hasRole('root')">
        <z:section>
            <jsp:attribute name="title">
                Withdrawn transcripts related to <zfin:abbrev entity="${relatedTranscriptDisplay.gene}" />
            </jsp:attribute>
            <jsp:body>
                <zfin2:markerTranscriptDataTable
                        transcripts="${relatedTranscriptDisplay.withdrawnTranscripts}"
                        unlinkedTranscript="${formBean.marker}"
                />
            </jsp:body>
        </z:section>
    </authz:authorize>
</c:forEach>
