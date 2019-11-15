<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="relatedTranscriptDisplay" required="true"
              rtexprvalue="true" type="org.zfin.marker.presentation.RelatedTranscriptDisplay" %>
<%@ attribute name="locations" required="false" type="java.util.Collection" %>
<%@ attribute name="showAllTranscripts" required="true"
              type="java.lang.Boolean" %>
<%@ attribute name="unlinkedTranscript" required="false"
              type="org.zfin.marker.Transcript" rtexprvalue="true"
              description="Don't make a link or show sequence tools for this transcript" %>
<%@ attribute name="title" required="false" rtexprvalue="true" type="java.lang.String"
              description="optional title, overrides default" %>

<script type="text/javascript">
    function showWithdrawnTranscripts(numWithdrawnTranscripts) {
        showWithDrawnTranscriptsHyperLink = document.getElementById('withdrawnTranscriptsLink');
        showWithDrawnTranscriptsHyperLink.style.display = 'none';
        hideWithDrawnTranscriptsHyperLink = document.getElementById('hideWithdrawnTranscriptsLink');
        hideWithDrawnTranscriptsHyperLink.style.display = 'inline';
        //window.alert(numWithdrawnTranscripts);


        for (var i = 0; i < numWithdrawnTranscripts; i++) {
            document.getElementById("withdrawnTranscripts-" + i).style.display = 'table-row';
        }

    }

    function hideWithdrawnTranscripts(numWithdrawnTranscripts) {
        showWithDrawnTranscriptsHyperLink = document.getElementById('withdrawnTranscriptsLink');
        showWithDrawnTranscriptsHyperLink.style.display = 'inline';
        hideWithDrawnTranscriptsHyperLink = document.getElementById('hideWithdrawnTranscriptsLink');
        hideWithDrawnTranscriptsHyperLink.style.display = 'none';
        //window.alert(numWithdrawnTranscripts);


        for (var i = 0; i < numWithdrawnTranscripts; i++) {
            document.getElementById("withdrawnTranscripts-" + i).style.display = 'none';
        }
    }
</script>

<div class="summary">
    <c:choose>
        <c:when test="${!empty relatedTranscriptDisplay.transcripts}">
            <table class="summary rowstripes_withdrawn">
                <c:set var="lastType" value=""/>
                <c:set var="groupIndex" value="0"/>
                <c:set var="lastTypeWithdrawn" value=""/>
                <c:forEach var="nonWithdrawnTranscript" items="${relatedTranscriptDisplay.nonWithdrawnTranscripts}"
                           varStatus="loop">


                    <c:if test="${ (showAllTranscripts) || (!showAllTranscripts && nonWithdrawnTranscripts.marker.transcriptType.display ne lastType) }">
                        <c:if test="${loop.first}">
                            <caption>
                                <c:choose>
                                    <c:when test="${!empty title}">
                                        ${title}
                                    </c:when>
                                    <c:otherwise>
                                        <zfin:link entity="${nonWithdrawnTranscript.otherMarker}"/> TRANSCRIPTS
                                    </c:otherwise>
                                </c:choose>

                            </caption>
                            <tr>

                                <th width="18%">Type <a class="popup-link info-popup-link"
                                                        href="/action/marker/transcript-types"></a></th>
                                <th width="22%">Name</th>
                                <th width="15%" class="length">Length (nt)</th>
                                <th width="25%" class="analysis">
                                    Analysis <a class="popup-link info-popup-link"
                                                href="/ZFIN/help_files/sequence_tools_help.html"></a>
                                </th>

                                <th width="20%"></th>
                            </tr>

                        </c:if>

                        <tr class=${loop.index%2==0 ? "even" : "odd"}>
                            <td width="18%"> <%-- only show if different from the last row--%>
                                <c:if test="${nonWithdrawnTranscript.marker.transcriptType.display ne lastType}">
                                    <span title="${nonWithdrawnTranscript.marker.transcriptType.definition}">${nonWithdrawnTranscript.marker.transcriptType.display}</span>
                                </c:if>
                                <c:set var="lastType" value="${nonWithdrawnTranscript.marker.transcriptType.display}"/>
                            </td>
                            <td width="22%">
                                <c:choose>
                                    <c:when test="${unlinkedTranscript ne null && unlinkedTranscript eq nonWithdrawnTranscript.marker}">
                                        <zfin:name entity="${nonWithdrawnTranscript.marker}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <zfin:link entity="${nonWithdrawnTranscript.marker}"/>
                                        <zfin:attribution entity="${nonWithdrawnTranscript}"/>
                                        <c:if test="${!empty nonWithdrawnTranscript.marker.ensdartId}">
                                            &nbsp;&nbsp;&nbsp;<a href="http://www.ensembl.org/id/${nonWithdrawnTranscript.marker.ensdartId}"><img src="/images/Ensembl.png" title="Ensembl" alt="Ensembl" border="0" align="top" class="scale" /></a>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="length" width="18%">
                                    ${nonWithdrawnTranscript.marker.length}
                            </td>
                            <td class="analysis" width="25%">
                                <c:if test="${empty nonWithdrawnTranscript}">
                                    no sequence available
                                </c:if>
                                <c:if test="${empty nonWithdrawnTranscript.displayedSequenceDBLinks}">
                                    <%--nonWithdrawnTranscript.displayedSequenceDBLinks is empty--%>
                                </c:if>
                                <c:if test="${unlinkedTranscript eq null || unlinkedTranscript ne nonWithdrawnTranscript.marker}">
                                    <c:choose>
                                        <c:when test="${fn:length(nonWithdrawnTranscript.displayedSequenceDBLinks) eq 1}">
                                            <zfin2:externalBlastDropDown
                                                    dbLink="${nonWithdrawnTranscript.displayedSequenceDBLinks[0]}"/>
                                        </c:when>
                                        <c:when test="${ fn:length(nonWithdrawnTranscript.displayedSequenceDBLinks) > 1}">
                                            (${fn:length(nonWithdrawnTranscript.displayedSequenceDBLinks)} sequences)
                                        </c:when>
                                    </c:choose>
                                </c:if>

                            </td>

                            <c:choose>
                                <c:when test="${loop.first}">
                                    <td class="gbrowseimage" width="20%"
                                        rowspan="${fn:length(relatedTranscriptDisplay.transcripts)}">

                                        <c:if test="${!empty relatedTranscriptDisplay.gbrowseImage}">
                                            <div class="gbrowse-image"/>
                                        </c:if>
                                    </td>
                                </c:when>
                                <c:otherwise>
                                </c:otherwise>
                            </c:choose>

                        </tr>
                    </c:if>
                </c:forEach>

                <c:if test="${relatedTranscriptDisplay.withdrawnTranscripts != null && fn:length(relatedTranscriptDisplay.withdrawnTranscripts) > 0}">
                    <authz:authorize access="hasRole('root')">
                        <tr class=${loop.index%2==0 ? "even" : "odd"}>
                            <td width="18%"><strong><a id="withdrawnTranscriptsLink" href="javascript:;"
                                                       onclick="showWithdrawnTranscripts(${fn:length(relatedTranscriptDisplay.withdrawnTranscripts)})"><img
                                    src="/images/plus-13.png" style="border:none;"
                                    title="show withdrawn transcripts"></a><a id="hideWithdrawnTranscriptsLink"
                                                                              style="display: none;" href="javascript:;"
                                                                              onclick="hideWithdrawnTranscripts(${fn:length(relatedTranscriptDisplay.withdrawnTranscripts)})"><img
                                    src="/images/minus-13.png" style="border:none;" title="hide withdrawn transcripts"></a>&nbsp;Withdrawn
                                Transcripts<img src="/images/warning-noborder.gif" border="0" alt="extinct" width="20"
                                                align="top" height="20"></strong></td>
                            <td width="22%"></td>
                            <td width="15%"></td>
                            <td width="25%"></td>
                            <td width="20%"></td>
                        </tr>

                        <c:forEach var="withdrawnTranscript" items="${relatedTranscriptDisplay.withdrawnTranscripts}"
                                   varStatus="withdrawnloop">
                            <tr id="withdrawnTranscripts-${withdrawnloop.index}" style="display: none;"
                                class=${withdrawnloop.index%2==0 ? "even" : "odd"}>
                                <td width="18%">
                                    <c:if test="${withdrawnTranscript.marker.transcriptType.display ne lastTypeWithdrawn}">
                                        <span title="${withdrawnTranscript.marker.transcriptType.definition}">${withdrawnTranscript.marker.transcriptType.display}</span>
                                    </c:if>
                                    <c:set var="lastTypeWithdrawn"
                                           value="${withdrawnTranscript.marker.transcriptType.display}"/>
                                </td>
                                <td width="22%">
                                    <zfin:link entity="${withdrawnTranscript.marker}"/><zfin:attribution
                                        entity="${withdrawnTranscript}"/>
                                </td>
                                <td class="length" width="15%">
                                        ${withdrawnTranscript.marker.length}
                                </td>
                                <td class="analysis" width="25%">
                                    <c:if test="${empty withdrawnTranscript}">
                                        no sequence available
                                    </c:if>
                                    <c:if test="${empty withdrawnTranscript.displayedSequenceDBLinks}">
                                        withdrawnTranscript.displayedSequenceDBLinks is empty
                                    </c:if>

                                    <c:choose>
                                        <c:when test="${fn:length(withdrawnTranscript.displayedSequenceDBLinks) eq 1}">
                                            <zfin2:externalBlastDropDown
                                                    dbLink="${withdrawnTranscript.displayedSequenceDBLinks[0]}"/>
                                        </c:when>
                                        <c:when test="${ fn:length(withdrawnTranscript.displayedSequenceDBLinks) > 1}">
                                            (${fn:length(withdrawnTranscript.displayedSequenceDBLinks)} sequences)
                                        </c:when>
                                    </c:choose>
                                </td>

                                <td width="20%">&nbsp;</td>
                            </tr>
                        </c:forEach>
                    </authz:authorize>
                </c:if>
            </table>
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
        </c:when>
        <c:otherwise>
            <b>${title}</b> <zfin2:noDataAvailable/>
        </c:otherwise>
    </c:choose>
</div>

<script>
    jQuery(".gbrowse-image").gbrowseImage({
        width: 300,
        imageUrl: "${relatedTranscriptDisplay.gbrowseImage.imageUrl}",
        linkUrl: "${relatedTranscriptDisplay.gbrowseImage.linkUrl}"
    });
</script>
