<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" scope="request" type="org.zfin.marker.presentation.GeneBean"/>

<z:attributeList>
    <zfin2:genomeBrowsersAttributeListItem locations="${formBean.transcriptLocations}" />
</z:attributeList>

<c:if test="${!empty formBean.relatedTranscriptDisplay.gbrowseImage}">
    <zfin-gbrowse:genomeBrowserImageComponent image="${formBean.relatedTranscriptDisplay.gbrowseImage}" />
</c:if>

<c:if test="${!empty formBean.relatedTranscriptDisplay.currentTranscripts}">
    <z:section title="Current Ensembl Transcripts (${formBean.relatedTranscriptDisplay.currentAssembliesDisplay})">
        <zfin2:markerCurrentTranscriptDataTable transcripts="${formBean.relatedTranscriptDisplay.currentTranscripts}"/>
    </z:section>
</c:if>

<z:section title="Older or Archived Transcripts (GRCz11)">
    <zfin2:markerTranscriptDataTable transcripts="${formBean.relatedTranscriptDisplay.archivedTranscripts}" showAssembly="true"/>
</z:section>

<authz:authorize access="hasRole('root')">
    <z:section title="Withdrawn Transcripts">
        <zfin2:markerTranscriptDataTable transcripts="${formBean.relatedTranscriptDisplay.withdrawnTranscripts}"/>
    </z:section>
</authz:authorize>
