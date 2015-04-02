<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="gbrowseImage" type="org.zfin.gbrowse.presentation.GBrowseImage" required="true"
              description="GBrowseImage object"%>
<%@ attribute name="width" required="false"%>

<c:if test="${!empty gbrowseImage}">
    <c:set var="domId" value="${zfn:generateRandomDomID()}"/>

    <a class="gbrowse_hover" rel="#popup-${domId}">GB</a>
    <div class="gbrowse_popup" id="popup-${domId}"
            data-gbrowse-image='{"imageUrl": "${gbrowseImage.imageUrl}", "linkUrl": "${gbrowseImage.linkUrl}"}'
    </div>

</c:if>