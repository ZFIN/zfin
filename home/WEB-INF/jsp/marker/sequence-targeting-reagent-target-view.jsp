<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<script src="/javascript/gbrowse-image.js"></script>

<div class="summaryTitle">Target Locations for <zfin:link entity="${formBean.marker}"/></div>

<c:forEach items="${formBean.gbrowseImages}" var="image">
    <div>
        <div class="gbrowse-image"
             data-gbrowse-image='{"imageUrl": "${image.imageUrl}", "linkUrl": "${image.linkUrl}", "build": "${image.build}"}'>
        </div>
    </div>
</c:forEach>

<script>
    jQuery(".gbrowse-image").gbrowseImage({width: 800});
</script>