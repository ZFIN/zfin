<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="relatedTranscriptDisplay" required="true"
              rtexprvalue="true" type="org.zfin.marker.presentation.RelatedTranscriptDisplay" %>
<%@ attribute name="showAllTranscripts" required="true"
              type="java.lang.Boolean" %>
<%@ attribute name="unlinkedTranscript" required="false"
              type="org.zfin.marker.Transcript" rtexprvalue="true"
              description="Don't make a link or show sequence tools for this transcript" %>
<%@ attribute name="title" required="false" rtexprvalue="true" type="java.lang.String"
              description="optional title, overrides default" %>
<a name="related_transcripts">


<script type="text/javascript">
    function showWithdrawnTranscripts(numWithdrawnTranscripts){
        showWithDrawnTranscriptsHyperLink = document.getElementById('withdrawnTranscriptsLink');
        showWithDrawnTranscriptsHyperLink.style.display = 'none' ;
        hideWithDrawnTranscriptsHyperLink = document.getElementById('hideWithdrawnTranscriptsLink');
        hideWithDrawnTranscriptsHyperLink.style.display = 'inline' ;
        //window.alert(numWithdrawnTranscripts);


        for(var i = 0; i < numWithdrawnTranscripts; i++) {
	    document.getElementById("withdrawnTranscripts-"+i).style.display = 'table-row' ;
        }

    }

    function hideWithdrawnTranscripts(numWithdrawnTranscripts) {
        showWithDrawnTranscriptsHyperLink = document.getElementById('withdrawnTranscriptsLink');
        showWithDrawnTranscriptsHyperLink.style.display = 'inline' ;
        hideWithDrawnTranscriptsHyperLink = document.getElementById('hideWithdrawnTranscriptsLink');
        hideWithDrawnTranscriptsHyperLink.style.display = 'none' ;
        //window.alert(numWithdrawnTranscripts);


       for(var i = 0; i < numWithdrawnTranscripts; i++) {
	    document.getElementById("withdrawnTranscripts-"+i).style.display = 'none' ;
       }
    }
</script>


<div class="summary">
    <c:choose>
        <c:when test="${!empty relatedTranscriptDisplay.transcripts}">
            <table class="summary largestripes relatedtranscripts">

                <c:set var="lastType" value=""/>
                <c:set var="groupIndex" value="0"/>
                <c:forEach var="nonWithdrawnTranscript" items="${relatedTranscriptDisplay.nonWithdrawnTranscripts}" varStatus="loop">


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

                                <th width="25%">Type<a class="popup-link info-popup-link" href="/action/marker/transcript-types"></a></th>
                                <th width="25%">Name</th>
                                <th style="width: 15%;" class="length">Length (bp)</th>
                                <th  style="width: 35%;" class="analysis">
                                    Analysis <a class="popup-link info-popup-link" href="/ZFIN/help_files/sequence_tools_help.html"></a>
                                </th>

                                <th width="30%"></th>
                            </tr>

                        </c:if>

                        <zfin:alternating-tr loopName="loop"
                                             groupBeanCollection="${relatedTranscriptDisplay.list}"
                                             groupByBean="marker.transcriptType.display">
                            <td> <%-- only show if different from the last row--%>
                                <zfin:groupByDisplay loopName="loop" groupBeanCollection="${relatedTranscriptDisplay.nonWithdrawnList}" groupByBean="marker.transcriptType.display">
                             <span title="${nonWithdrawnTranscript.marker.transcriptType.definition}">
                                     ${nonWithdrawnTranscript.marker.transcriptType.display}</span>
                                </zfin:groupByDisplay>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${unlinkedTranscript ne null && unlinkedTranscript eq nonWithdrawnTranscript.marker}">
                                        <zfin:name entity="${nonWithdrawnTranscript.marker}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <zfin:link entity="${nonWithdrawnTranscript.marker}"/>
                                        <zfin:attribution entity="${nonWithdrawnTranscript}"/>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="length">
                                    ${nonWithdrawnTranscript.marker.length}
                            </td>
                            <td class="analysis">
                                <c:if test="${empty nonWithdrawnTranscript}">
                                    no sequence available
                                </c:if>
                                <c:if test="${empty nonWithdrawnTranscript.displayedSequenceDBLinks}">
                                    nonWithdrawnTranscript.displayedSequenceDBLinks is empty
                                </c:if>
                                <c:if test="${unlinkedTranscript eq null || unlinkedTranscript ne nonWithdrawnTranscript.marker}">
                                    <c:choose>
                                        <c:when test="${fn:length(nonWithdrawnTranscript.displayedSequenceDBLinks) eq 1}">
                                            <zfin2:externalBlastDropDown dbLink="${nonWithdrawnTranscript.displayedSequenceDBLinks[0]}"/>
                                        </c:when>
                                        <c:when test="${ fn:length(nonWithdrawnTranscript.displayedSequenceDBLinks) > 1}">
                                            (${fn:length(nonWithdrawnTranscript.displayedSequenceDBLinks)} sequences)
                                        </c:when>
                                    </c:choose>
                                </c:if>

                            </td>

                            <c:if test="${loop.first}">
                                <td class="gbrowseimage"
                                    rowspan="${fn:length(relatedTranscriptDisplay.transcripts)}">

                                    <c:if test="${!empty relatedTranscriptDisplay.gbrowseImages}">

                                        <zfin2:gbrowseImageStack gbrowseImages="${relatedTranscriptDisplay.gbrowseImages}"/>
                                    </c:if>
                                </td>
                            </c:if>
                        </zfin:alternating-tr>
                    </c:if>                                
                </c:forEach>

       <c:if test="${relatedTranscriptDisplay.withdrawnTranscripts != null && fn:length(relatedTranscriptDisplay.withdrawnTranscripts) > 0}">
                <tr><td><strong><a id="withdrawnTranscriptsLink" href="javascript:;" onclick="showWithdrawnTranscripts(${fn:length(relatedTranscriptDisplay.withdrawnTranscripts)})"><img src="/images/plus-13.png" style="border:none;" title="show withdrawn transcripts"></a><a id="hideWithdrawnTranscriptsLink" style="display: none;" href="javascript:;" onclick="hideWithdrawnTranscripts(${fn:length(relatedTranscriptDisplay.withdrawnTranscripts)})"><img src="/images/minus-13.png" style="border:none;" title="hide withdrawn transcripts"></a>&nbsp;Withdrawn Transcripts<img src="/images/warning-noborder.gif" border="0" alt="extinct" width="20" align="top" height="20"></strong></td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                </tr>      
                
                <c:forEach var="withdrawnTranscript" items="${relatedTranscriptDisplay.withdrawnTranscripts}" varStatus="withdrawnloop">

                        <tr id ="withdrawnTranscripts-${withdrawnloop.index}" style="display: none;">
                            <td>
                             <zfin:groupByDisplay loopName="withdrawnloop" groupBeanCollection="${relatedTranscriptDisplay.withdrawnList}" groupByBean="marker.transcriptType.display">
			       <span title="${withdrawnTranscript.marker.transcriptType.definition}">${withdrawnTranscript.marker.transcriptType.display}</span>
                             </zfin:groupByDisplay>
                            </td>
                            <td>
                               <zfin:link entity="${withdrawnTranscript.marker}"/><zfin:attribution entity="${withdrawnTranscript}"/>
                            </td>
                            <td class="length">
                                   ${withdrawnTranscript.marker.length}
                            </td>
                            <td class="analysis">
                                <c:if test="${empty withdrawnTranscript}">
                                    no sequence available
                                </c:if>
                                <c:if test="${empty withdrawnTranscript.displayedSequenceDBLinks}">
                                    withdrawnTranscript.displayedSequenceDBLinks is empty
                                </c:if>

                                    <c:choose>
                                        <c:when test="${fn:length(withdrawnTranscript.displayedSequenceDBLinks) eq 1}">
                                            <zfin2:externalBlastDropDown dbLink="${withdrawnTranscript.displayedSequenceDBLinks[0]}"/>
                                        </c:when>
                                        <c:when test="${ fn:length(withdrawnTranscript.displayedSequenceDBLinks) > 1}">
                                            (${fn:length(withdrawnTranscript.displayedSequenceDBLinks)} sequences)
                                        </c:when>
                                    </c:choose>
                            </td>

                            <c:if test="${withdrawnloop.first}">
                                <td class="gbrowseimage"
                                    rowspan="${fn:length(relatedTranscriptDisplay.transcripts)}">

                                    &nbsp;
                                </td>
                            </c:if>
                        </tr>
                </c:forEach>
       </c:if> 

            </table>
        </c:when>
        <c:otherwise>
            <b>${title}</b> <zfin2:noDataAvailable/>
        </c:otherwise>
    </c:choose>
</div>

