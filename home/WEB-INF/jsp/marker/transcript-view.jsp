<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="/action/marker/marker-edit?zdbID=${formBean.marker.zdbID}"
                   deleteURL="none"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:transcriptHead transcript="${formBean.marker}" previousNames="${formBean.previousNames}"
                      relatedGenes="${formBean.relatedGenes}" strain="${formBean.strain}"/>

<c:choose>
    <c:when test="${formBean.marker.withdrawn}">
        <authz:authorize access="hasRole('root')">
            <zfin2:sequenceView sequences="${formBean.nucleotideSequences}"/>
        </authz:authorize>
    </c:when>
    <c:otherwise>
        <zfin2:sequenceView sequences="${formBean.nucleotideSequences}"/>
    </c:otherwise>
</c:choose>


<c:if test="${!empty formBean.unableToFindDBLinks}">
    <div class="error-inline">Unable to retrieve the following
            ${(fn:length(formBean.unableToFindDBLinks) eq 1) ? 'sequence' : 'sequences'}:
        <c:forEach var="unableToFindDBLink" items="${formBean.unableToFindDBLinks}">
            <zfin:link entity="${unableToFindDBLink}"/>
            <zfin:attribution entity="${unableToFindDBLink}"/>
        </c:forEach>
    </div>
</c:if>


<%--<zfin2:subsection title="Non-Reference Strains"
                  inlineTitle="true">
                  &lt;%&ndash;test="${!empty formBean.nonReferenceStrains}" showNoData="true">&ndash;%&gt;
    <zfin2:toggledHyperlinkList collection="${formBean.nonReferenceStrains}"
                                id="nonRefererenceStrains"
                                maxNumber="6"/>
</zfin2:subsection>--%>

<%-- This section shows a flat list of related transcripts--%>
<c:if test="${formBean.marker.transcriptType.display eq 'miRNA'}">
    <zfin2:subsection title="Related Transcripts" test="${!empty formBean.microRNARelatedTranscripts}"
                      inlineTitle="true" showNoData="true">
        <zfin2:toggledHyperlinkList collection="${formBean.microRNARelatedTranscripts}"
                                    id="microRNARelatedTranscripts"
                                    maxNumber="6"/>
    </zfin2:subsection>
</c:if>

<%-- This section shows boxes per gene.  One data structure or the other should get populated,
     but not both.  --%>

<c:forEach var="relatedTranscriptDisplay" items="${formBean.relatedTranscriptDisplayList}">
    <c:if test="${fn:length(relatedTranscriptDisplay.transcripts) > 1}">
        <zfin2:markerTranscriptSummary relatedTranscriptDisplay="${relatedTranscriptDisplay}"
                                       unlinkedTranscript="${formBean.marker}"
                                       showAllTranscripts="true"/>
    </c:if>


    <%--"no siblings" gbrowse image --%>
    <c:if test="${(fn:length(relatedTranscriptDisplay.transcripts) == 1) && (!empty relatedTranscriptDisplay.gbrowseImage) }">

        <script src="/javascript/gbrowse-image.js"></script>
        <div class="summary" id="single-transcript-gbrowse-section">
            <table class="summary solidblock">
                <caption>GBrowse:</caption>
                <tr>
                    <td style="text-align: center">
                        <div class="gbrowse-image" />
                    </td>
                </tr>
            </table>
        </div>
        <script>
            jQuery("#single-transcript-gbrowse-section").gbrowseImage({
                width: 600,
                imageTarget: ".gbrowse-image",
                imageUrl: "${relatedTranscriptDisplay.gbrowseImage.imageUrl}",
                linkUrl: "${relatedTranscriptDisplay.gbrowseImage.linkUrl}"
            });
        </script>


    </c:if>
</c:forEach>


<zfin2:markerRelationships relationships="${formBean.markerRelationships}" marker="${formBean.marker}"
                           title="SEGMENT (CLONE AND PROBE) RELATIONSHIPS"/>

<c:if test="${formBean.marker.transcriptType.display eq 'miRNA'}">
    <zfin2:transcriptTargets transcriptTargets="${formBean.transcriptTargets}"/>
</c:if>

<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}"/>

<zfin2:markerSummaryDBLinkDisplay marker="${formBean.marker}" links="${formBean.proteinProductDBLinkDisplay}"
                                  title="PROTEIN PRODUCTS"/>

<zfin2:transcriptSequenceInformation sequenceInfo="${formBean.sequenceInfo}" title="ZEBRAFISH SUPPORTING SEQUENCES"
                                     showAllSequences="true"/>

<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<br>


