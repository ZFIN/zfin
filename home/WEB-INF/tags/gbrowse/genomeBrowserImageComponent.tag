<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="image" type="org.zfin.genomebrowser.presentation.GenomeBrowserImage" required="true"
              description="GenomeBrowserImage object" %>
<%@ attribute name="loopIndex" required="false" %>

<c:set var="loopIndex" value="${(empty loopIndex) ? 0 : loopIndex}" />

<c:if test="${not empty image}">
    <div class="__react-root genome-browser-image" id="${image.reactComponentId}__${loopIndex}"
         data-image-url="${image.imageUrl}"
         data-link-url="${image.linkUrl}"
         data-build="${image.build}"
         data-chromosome="${image.chromosome}"
         data-height="${image.height}"
    ></div>
</c:if>
<c:if test="${empty image}">
    <div class="__react-root genome-browser-image" id="GbrowseImage"
         data-image-url="${image.imageUrl}"
         data-link-url="${image.linkUrl}"
         data-build="${image.build}"
         data-chromosome="${image.chromosome}"
    ></div>
</c:if>