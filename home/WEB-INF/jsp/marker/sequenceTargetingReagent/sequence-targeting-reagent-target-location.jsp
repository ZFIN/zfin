<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<c:if test="${!empty formBean.gbrowseImages}">

    <c:forEach items="${formBean.gbrowseImages}" var="image" end="1">
        <div class="gbrowse-image-prototype"
             data-gbrowse-image='{"imageUrl": "${image.imageUrl}", "linkUrl": "${image.linkUrl}", "build": "${image.build}"}'>
        </div>
    </c:forEach>

    <c:if test="${fn:length(formBean.gbrowseImages) > 2}">
        <div>
            <a href="/action/marker/view/${formBean.marker.zdbID}/str-targeted-genes">View
                all ${fn:length(formBean.gbrowseImages)} target locations</a>
        </div>
    </c:if>

</c:if>

<script>
    $(".gbrowse-image-prototype").gbrowseImage({
        width: 700,
        imageUrl: "${formBean.relatedTranscriptDisplay.gbrowseImage.imageUrl}",
        linkUrl: "${formBean.relatedTranscriptDisplay.gbrowseImage.linkUrl}",
        build: "${formBean.relatedTranscriptDisplay.gbrowseImage.build}"
    });
</script>

<c:forEach items="${formBean.gbrowseImages}" var="image" end="1">
    <div class="gbrowse-image-prototype"
         data-gbrowse-image='{"imageUrl": "${image.imageUrl}", "linkUrl": "${image.linkUrl}", "build": "${image.build}"}'>
    </div>
</c:forEach>

<c:if test="${fn:length(formBean.gbrowseImages) > 2}">
    <div>
        <a href="/action/marker/view/${formBean.marker.zdbID}/str-targeted-genes">View
            all ${fn:length(formBean.gbrowseImages)} target locations</a>
    </div>
</c:if>