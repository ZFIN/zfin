<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>


<z:dataTable collapse="true" hasData="${!empty formBean.relatedTranscriptDisplayList}">

    <thead>
    <tr>
        <th>Type <a class="popup-link info-popup-link"
                    href="/action/marker/transcript-types"></a></th>
        <th>Name</th>
        <th>Length</th>
        <th>
            Analysis <a class="popup-link info-popup-link"
                        href="/ZFIN/help_files/sequence_tools_help.html"></a>
        </th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="transcriptDisplay" items="${formBean.relatedTranscriptDisplayList}"
               varStatus="loop">
        <c:forEach var="transcript" items="${transcriptDisplay.withdrawnTranscripts}"
                   varStatus="loop">
            <tr>
                <td>
                    <span title="${transcript.marker.transcriptType.definition}">${transcript.marker.transcriptType.display}</span>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${formBean.marker ne null && formBean.marker eq transcript.marker}">
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
                <td>${transcript.marker.length} nt
                </td>
                <td>
                    <c:if test="${empty transcript}">
                        no sequence available
                    </c:if>
                    <c:if test="${formBean.marker eq null || formBean.marker ne transcript.marker}">
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
            </tr>
        </c:forEach>
    </c:forEach>
    </tbody>
</z:dataTable>



