<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="transcripts" required="true"
    rtexprvalue="true" type="org.zfin.marker.presentation.RelatedTranscriptDisplay" %>
<%@ attribute name="showAllTranscripts" required="true"
        type="java.lang.Boolean" %>
<%@ attribute name="unlinkedTranscript" required="false"
              type="org.zfin.marker.Transcript" rtexprvalue="true"
              description="Don't make a link or show sequence tools for this transcript" %>
<%@ attribute name="title" required="false" rtexprvalue="true" type="java.lang.String"
        description="optional title, overrides default" %>


<c:if test="${!empty transcripts}">
    <div class="summary">
    <table class="summary largestripes relatedtranscripts">

        <c:set var="lastType" value=""/>
        <c:set var="groupIndex" value="0"/>
        <c:forEach var="relatedTranscript" items="${transcripts}" varStatus="loop">


        <c:if test="${ (showAllTranscripts) || (!showAllTranscripts && relatedTranscript.marker.transcriptType.display ne lastType) }">
            <c:if test="${loop.first}">
                <caption>
                <c:choose>
                    <c:when test="${!empty title}">
                        ${title}
                    </c:when>
                    <c:otherwise>
                        <zfin:link entity="${relatedTranscript.otherMarker}"/> TRANSCRIPTS:
                    </c:otherwise>
                </c:choose>

                </caption>
                <tr>
                    <th width="25%"><a href="/action/marker/transcript-definitions#type">Type</a></th>
                    <th width="25%">Name</th>
                    <th style="width: 15%;" class="length">Length (bp)</th>
                    <th  style="width: 35%;" class="analysis">
                        <a href="javascript:;"
                           onclick="top.zfinhelp=open('/<zfin:webdriver/>?MIval=aa-helpframes.html&calling_page=sequence_tools_help.html','helpwindow','scrollbars=yes,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=800,height=600');">
                        <b>Analysis</b></a>
                    </th>
                    
                </tr>

            </c:if>

            <zfin:alternating-tr loopName="loop"
                                 groupBeanCollection="${transcripts.list}"
                                 groupByBean="marker.transcriptType.display">
                <td> <%-- only show if different from the last row--%>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${transcripts.list}" groupByBean="marker.transcriptType.display">
                        <span title="${relatedTranscript.marker.transcriptType.definition}">${relatedTranscript.marker.transcriptType.display}</span>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    <c:choose>
                      <c:when test="${unlinkedTranscript ne null && unlinkedTranscript eq relatedTranscript.marker}">
                          <zfin:name entity="${relatedTranscript.marker}"/>
                      </c:when>
                      <c:otherwise>
                          <zfin:link entity="${relatedTranscript.marker}"/>
                          <zfin:attribution entity="${relatedTranscript}"/>
                       </c:otherwise>
                    </c:choose>

                </td>
                <td class="length">
                    ${relatedTranscript.marker.length}
                </td>
                <td class="analysis">
                    <c:if test="${empty relatedTranscript}">
                        related transcript is empty
                    </c:if>
                   <c:if test="${empty relatedTranscript.displayedSequenceDBLinks}">
                          relatedTranscript.displayedSequenceDBLinks is empty
                    </c:if>
                  <c:if test="${unlinkedTranscript eq null || unlinkedTranscript ne relatedTranscript.marker}">
                        <c:choose>
                            <c:when test="${fn:length(relatedTranscript.displayedSequenceDBLinks) eq 1}">
                                <zfin2:externalBlastDropDown dbLink="${relatedTranscript.displayedSequenceDBLinks[0]}"/>
                            </c:when>
                            <c:when test="${ fn:length(relatedTranscript.displayedSequenceDBLinks) > 1}">
                           (${fn:length(relatedTranscript.displayedSequenceDBLinks)} sequences)
                            </c:when>
                        </c:choose>
                  </c:if>
                
                </td>
            </zfin:alternating-tr>
        </c:if>
        </c:forEach>

    </table>
    </div>
</c:if>