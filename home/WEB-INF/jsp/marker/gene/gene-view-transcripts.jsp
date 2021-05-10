<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" scope="request" type="org.zfin.marker.presentation.GeneBean"/>

<z:attributeList>
    <zfin2:genomeBrowsersAttributeListItem locations="${formBean.locations}" />
</z:attributeList>

<c:if test="${!empty formBean.relatedTranscriptDisplay.gbrowseImage}">
    <div class="__react-root"
         id="GbrowseImage"
         data-image-url="${formBean.relatedTranscriptDisplay.gbrowseImage.imageUrl}"
         data-link-url="${formBean.relatedTranscriptDisplay.gbrowseImage.linkUrl}"
         data-build="${formBean.relatedTranscriptDisplay.gbrowseImage.build}">
    </div>
</c:if>

<z:section>
    <zfin2:markerTranscriptDataTable transcripts="${formBean.relatedTranscriptDisplay.nonWithdrawnTranscripts}"/>
</z:section>

<authz:authorize access="hasRole('root')">
    <z:section title="Withdrawn Transcripts">
        <zfin2:markerTranscriptDataTable transcripts="${formBean.relatedTranscriptDisplay.withdrawnTranscripts}"/>
    </z:section>
</authz:authorize>
