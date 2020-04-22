<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<c:forEach var="relatedTranscriptDisplay" items="${formBean.relatedTranscriptDisplayList}" varStatus="loop">
    <c:if test="${(fn:length(relatedTranscriptDisplay.transcripts) == 1) && (!empty relatedTranscriptDisplay.gbrowseImage) }">
        <div class="__react-root"
             id="GbrowseImage__${loop.index}"
             data-image-url="${relatedTranscriptDisplay.gbrowseImage.imageUrl}"
             data-link-url="${relatedTranscriptDisplay.gbrowseImage.linkUrl}"
             data-build="${relatedTranscriptDisplay.gbrowseImage.build}">
        </div>
    </c:if>
</c:forEach>
