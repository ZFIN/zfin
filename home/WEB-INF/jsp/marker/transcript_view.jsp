<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${formBean.editURL}"
                   deleteURL="${formBean.deleteURL}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker"/>

<div style="float: right">
    <tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:put name="subjectName" value="${formBean.marker.name}"/>
        <tiles:put name="subjectID" value="${formBean.marker.zdbID}"/>
    </tiles:insert>
</div>

<zfin2:transcriptHead transcript="${formBean.marker}"/>

<script type="text/javascript" src="/javascript/sequenceview.js">
</script>


<zfin2:sequenceView sequences="${formBean.nucleotideSequences}"/>


<c:if test="${!empty formBean.unableToFindDBLinks}">
    <div class="error-inline">Unable to retrieve the following
            ${(fn:length(formBean.unableToFindDBLinks) eq 1) ? 'sequence' : 'sequences'}:
        <c:forEach var="unableToFindDBLink"  items="${formBean.unableToFindDBLinks}">
            <zfin:link entity="${unableToFindDBLink}"/>
            <zfin:attribution entity="${unableToFindDBLink}"/>
        </c:forEach>
    </div>
</c:if>

<%--
<zfin2:markerGbrowse marker="${formBean.marker}"/>
--%>



<zfin2:subsection title="Associated with Genes"
                  inlineTitle="true"
                  test="${!empty formBean.relatedGenes}">
    <zfin2:toggledHyperlinkList collection="${formBean.relatedGenes}"
                                id="relatedGenes"
                                maxNumber="6"
                                showAttributionLinks="true"/>
</zfin2:subsection>

<zfin2:subsection title="Non-Reference Strains"
                  inlineTitle="true"
                  test="${!empty formBean.nonReferenceStrains}">
    <zfin2:toggledHyperlinkList collection="${formBean.nonReferenceStrains}"
                                id="nonRefererenceStrains"
                                maxNumber="6"/>
</zfin2:subsection>


<zfin2:markerRelationships relationships="${formBean.transcriptRelationships}" marker="${formBean.marker}"
                           title="${fn:toUpperCase('Segment (Clone and Probe) Relationships')}" />
<%-- This section shows a flat list of related transcripts--%>

<zfin2:subsection title="Related Transcripts" test="${!empty formBean.microRNARelatedTranscripts}"
                  inlineTitle="true" >
    <zfin2:toggledHyperlinkList collection="${formBean.microRNARelatedTranscripts}"
                                id="microRNARelatedTranscripts"
                                maxNumber="6"/>
</zfin2:subsection>


<%-- This section shows boxes per gene.  One data structure or the other should get populated,
     but not both.  --%>

<c:forEach var="relatedTranscriptDisplay" items="${formBean.relatedTranscriptDisplayList}">
    <c:if test="${fn:length(relatedTranscriptDisplay) > 1}">
        <zfin2:markerTranscriptSummary transcripts="${relatedTranscriptDisplay}"
                                       unlinkedTranscript="${formBean.marker}"
                                       showAllTranscripts="true"/>
    </c:if>
</c:forEach>

<zfin2:transcriptTargets transcriptTargets="${formBean.transcriptTargets}"/>

<zfin2:markerSummaryPages marker="${formBean.marker}" links="${formBean.summaryDBLinkDisplay}"/>

<zfin2:markerSummaryPages marker="${formBean.marker}" links="${formBean.proteinProductDBLinkDisplay}" title="${fn:toUpperCase('Protein Products')}" />



<zfin2:transcriptSequenceInformation sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Zebrafish Supporting Sequences')}" showAllSequences="true"/>


<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<br>


