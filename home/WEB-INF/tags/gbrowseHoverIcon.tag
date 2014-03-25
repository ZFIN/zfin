<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="gbrowseImage" type="org.zfin.gbrowse.presentation.GBrowseImage" required="true"
              description="GBrowseImage object"%>
<%@ attribute name="width" required="false"%>

<c:if test="${!empty gbrowseImage}">
  <a class="gbrowse_hover">GB</a>
  <div class="gbrowse_popup"><zfin2:gbrowseImageStack gbrowseImage="${gbrowseImage}"/></div>
  <script> jQuery(document).ready(function() { jQuery('.gbrowse_hover').tooltip().dynamic({ bottom: { direction: 'down'}}); }); </script>

</c:if>