<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="transcripts" required="true" type="java.util.Collection" %>
<%@ attribute name="unlinkedTranscript" required="false" type="org.zfin.marker.Marker" %>

<z:dataTable collapse="true" hasData="${!empty transcripts}">
    <thead>
        <tr>
            <th>Type <a class="popup-link info-popup-link" href="/action/marker/transcript-types"></a></th>
            <th>Name</th>
            <th class="text-right">Length (nt)</th>
            <th>Analysis <a class="popup-link info-popup-link" href="/ZFIN/help_files/sequence_tools_help.html"></a></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="transcript" items="${transcripts}">
            <tr>
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
                                <z:otherPagesDropdown>
                                    <zfin2:externalLink className="dropdown-item" href="http://www.ensembl.org/id/${transcript.marker.ensdartId}">Ensembl</zfin2:externalLink>
                                </z:otherPagesDropdown>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="text-right">${transcript.marker.length} nt</td>
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
            </tr>
        </c:forEach>
    </tbody>
</z:dataTable>