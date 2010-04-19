<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="gbrowseImages" type="java.util.List" required="true"
              description="List of GBrowseImage objects"%>
<%@ attribute name="width" required="false"%>

<c:if test="${!empty gbrowseImages}">
  <a class="gbrowse_hover">
      GB
      <span class="gbrowse_popup"><zfin2:gbrowseImageStack gbrowseImages="${gbrowseImages}"/></span>
  </a>

</c:if>