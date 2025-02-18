<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@ attribute name="image" type="org.zfin.genomebrowser.presentation.GenomeBrowserImage" required="true"
              description="GenomeBrowserImage object" %>
<%@ attribute name="loopIndex" required="false" %>
<%@ attribute name="themeColor" required="false" %>

<c:set var="loopIndex" value="${(empty loopIndex) ? 0 : loopIndex}" />
<c:set var="themeColor" value="${ZfinPropertiesEnum.PRIMARY_COLOR.value()}" />

<c:if test="${not empty image}">
    <div class="__react-root genome-browser-image" id="${image.reactComponentId}__${loopIndex}"
         data-image-url="${image.imageUrl}"
         data-link-url="${image.linkUrl}"
         data-build="${image.build}"
         data-chromosome="${image.chromosome}"
         data-height="${image.height}"
         data-landmark="${image.landmark}"
         data-color="${themeColor}"
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