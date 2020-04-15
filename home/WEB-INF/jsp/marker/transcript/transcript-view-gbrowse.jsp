<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>


<c:if test="${!empty formBean.relatedTranscriptDisplay.gbrowseImage}">

    <div class="gbrowse-image-prototype"></div>

</c:if>

<script>
    $(".gbrowse-image-prototype").gbrowseImage({
        width: 700,
        imageUrl: "${formBean.relatedTranscriptDisplay.gbrowseImage.imageUrl}",
        linkUrl: "${formBean.relatedTranscriptDisplay.gbrowseImage.linkUrl}",
        build: "${formBean.relatedTranscriptDisplay.gbrowseImage.build}"
    });
</script>

