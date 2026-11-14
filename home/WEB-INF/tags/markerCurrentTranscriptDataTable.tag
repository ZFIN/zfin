<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Ensembl transcripts annotated on a current assembly (GRCz12tu, GRCz13ab, GRCz12ab, ...).
     These are named by their ENSDART accession, so the name is plain text, not a link. --%>
<%@ attribute name="transcripts" required="true" type="java.util.Collection" %>

<z:dataTable collapse="true" hasData="${!empty transcripts}">
    <thead>
        <tr>
            <th>Type <a class="popup-link info-popup-link" href="/action/marker/transcript-types"></a></th>
            <th>Name</th>
            <th>Accession</th>
            <th>Genome Assembly</th>
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
                <td>${transcript.marker.ensdartId}</td>
                <td>
                    <c:if test="${!empty transcript.marker.ensdartId}">
                        <zfin2:externalLink href="http://www.ensembl.org/id/${transcript.marker.ensdartId}">${transcript.marker.ensdartId}</zfin2:externalLink>
                    </c:if>
                </td>
                <td>${fn:join(transcript.assemblyNames.toArray(), ', ')}</td>
                <td class="text-right"><fmt:formatNumber type="number" maxFractionDigits="2" value="${transcript.marker.length}"/> nt</td>
                <td>
                    <c:if test="${fn:length(transcript.displayedSequenceDBLinks) eq 1}">
                        <zfin2:blastDropDown dbLink="${transcript.displayedSequenceDBLinks[0]}"/>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</z:dataTable>
