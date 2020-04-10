<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>


<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Previous Names">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                <li>${markerAlias.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Type">
        <zfin2:externalLink
                href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">${formBean.zfinSoTerm.termName}</zfin2:externalLink>
    </z:attributeListItem>

    <z:attributeListItem label="Annotation Status">
        ${formBean.transcript.status.display}
    </z:attributeListItem>

<z:attributeListItem label="Associated With Genes">
    <zfin2:toggledLinkList collection="${formBean.relatedGenes}"
                           maxNumber="6"
                           showAttributionLinks="true"/>
</z:attributeListItem>
    <z:attributeListItem label="Strain">
        <zfin:link entity="${formBean.strain}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Non Reference Strain">
        <zfin2:toggledLinkList collection="${formBean.nonReferenceStrains}"
                               maxNumber="6"/>
    </z:attributeListItem>

    <z:attributeListItem label="Citations">
        <a href="/action/marker/citation-list/${formBean.marker.zdbID}">(${formBean.numPubs})</a>
    </z:attributeListItem>

</z:attributeList>

    <%--&lt;%&ndash;"no siblings" gbrowse image &ndash;%&gt;--%>
    <%--<c:if test="${(fn:length(relatedTranscriptDisplay.transcripts) == 1) && (!empty relatedTranscriptDisplay.gbrowseImage) }">--%>
        <%--<div class="summary" id="single-transcript-gbrowse-section">--%>
            <%--<table class="summary solidblock">--%>
                <%--<caption>GBrowse:</caption>--%>
                <%--<tr>--%>
                    <%--<td style="text-align: center">--%>
                        <%--<div class="gbrowse-image" />--%>
                    <%--</td>--%>
                <%--</tr>--%>
            <%--</table>--%>
        <%--</div>--%>
        <%--<script>--%>
            <%--jQuery("#single-transcript-gbrowse-section").gbrowseImage({--%>
                <%--width: 600,--%>
                <%--imageTarget: ".gbrowse-image",--%>
                <%--imageUrl: "${relatedTranscriptDisplay.gbrowseImage.imageUrl}",--%>
                <%--linkUrl: "${relatedTranscriptDisplay.gbrowseImage.linkUrl}"--%>
            <%--});--%>
        <%--</script>--%>
    <%--</c:if>--%>
<%--</c:forEach>--%>

<c:if test="${formBean.marker.transcriptType.display eq 'miRNA'}">
    <zfin2:transcriptTargets transcriptTargets="${formBean.transcriptTargets}"/>
</c:if>
<br>
<c:if test="${formBean.rnaCentralLink eq 'yes'}">
    <a href=""><b>RNACentral</b></a>
</c:if>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}"/>

