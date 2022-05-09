<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${not empty formBean.GBrowseImage}">
    <div class="__react-root genome-browser-image"
         id="${formBean.GBrowseImage.reactComponentId}"
         data-image-url="${formBean.GBrowseImage.imageUrl}"
         data-link-url="${formBean.GBrowseImage.linkUrl}"
         data-build="${formBean.GBrowseImage.build}"
    ></div>
</c:if>
<c:if test="${empty formBean.GBrowseImage}">
    <div class="__react-root genome-browser-image" id="GbrowseImage"
         data-image-url="${formBean.GBrowseImage.imageUrl}"
         data-link-url="${formBean.GBrowseImage.linkUrl}"
         data-build="${formBean.GBrowseImage.build}"
    ></div>
</c:if>
