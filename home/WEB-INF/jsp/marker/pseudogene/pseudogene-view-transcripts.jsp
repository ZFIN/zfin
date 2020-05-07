<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${!empty formBean.relatedTranscriptDisplay.gbrowseImage}">
    <div class="__react-root"
         id="GbrowseImage"
         data-image-url="${formBean.relatedTranscriptDisplay.gbrowseImage.imageUrl}"
         data-link-url="${formBean.relatedTranscriptDisplay.gbrowseImage.linkUrl}"
         data-build="${formBean.relatedTranscriptDisplay.gbrowseImage.build}">
    </div>
</c:if>

<z:dataTable collapse="true" hasData="${!empty formBean.relatedTranscriptDisplay.nonWithdrawnTranscripts}">

    <thead>
    <tr>
        <th>Type <a class="popup-link info-popup-link"
                                href="/action/marker/transcript-types"></a></th>
        <th>Name</th>
        <th style="text-align: right">Length (nt)</th>
        <th>
            Analysis <a class="popup-link info-popup-link"
                        href="/ZFIN/help_files/sequence_tools_help.html"></a>
        </th>
    </tr>
    </thead>
    <c:forEach var="transcript" items="${formBean.relatedTranscriptDisplay.nonWithdrawnTranscripts}" varStatus="loop">
        <tbody>
        <td>
            <span title="${transcript.marker.transcriptType.definition}">${transcript.marker.transcriptType.display}</span>
        </td>
        <td>
            <c:choose>
                <c:when test="${unlinkedTranscript ne null && unlinkedTranscript eq transcript.marker}">
                    <zfin:name entity="${transcript.marker}"/>
                </c:when>
                <c:otherwise>
                    <zfin:link entity="${transcript.marker}"/>
                    <zfin:attribution entity="${transcript}"/>
                    <c:if test="${!empty transcript.marker.ensdartId}">
                        &nbsp;&nbsp;&nbsp;<a href="http://www.ensembl.org/id/${transcript.marker.ensdartId}"><img src="/images/Ensembl.png" title="Ensembl" alt="Ensembl" border="0" align="top" class="scale" /></a>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </td>
        <td style="text-align: right">
                ${transcript.marker.length} nt
        </td>
        <td>
            <c:if test="${empty transcript}">
                no sequence available
            </c:if>
            <c:if test="${empty transcript.displayedSequenceDBLinks}">
                <%--nonWithdrawnTranscript.displayedSequenceDBLinks is empty--%>
            </c:if>
            <c:if test="${unlinkedTranscript eq null || unlinkedTranscript ne transcript.marker}">
                <c:choose>
                    <c:when test="${fn:length(transcript.displayedSequenceDBLinks) eq 1}">
                        <zfin2:blastDropDown
                                dbLink="${transcript.displayedSequenceDBLinks[0]}"/>
                    </c:when>
                    <c:when test="${ fn:length(transcript.displayedSequenceDBLinks) > 1}">
                        (${fn:length(transcript.displayedSequenceDBLinks)} sequences)
                    </c:when>
                </c:choose>
            </c:if>
        </td>
    </c:forEach>
    </tbody>
</z:dataTable>

<c:if test="${!empty locations}">
    <table>
        <tfoot>
        <tr>
            <td colspan="3">
                <strong>Browsers:</strong>
                <c:forEach var="location" items="${locations}" varStatus="loop">
                    <a href="${location.url}">${location.name}</a><c:if test="${!loop.last}">,&nbsp;</c:if>
                </c:forEach>
            </td>
        </tr>
        </tfoot>
    </table>
</c:if>



