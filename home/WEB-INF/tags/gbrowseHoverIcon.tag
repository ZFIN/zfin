<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="gbrowseImages" type="java.util.List" required="true"
              description="List of GBrowseImage objects"%>
<%@ attribute name="width" required="false"%>

<c:if test="${!empty gbrowseImages}">
  <a class="gbrowse_hover">GB</a>
  <div class="gbrowse_popup"><zfin2:gbrowseImageStack gbrowseImages="${gbrowseImages}"/></div>
  <script> jQuery(document).ready(function() { jQuery('.gbrowse_hover').tooltip().dynamic({ bottom: { direction: 'down'}}); }); </script>

</c:if>