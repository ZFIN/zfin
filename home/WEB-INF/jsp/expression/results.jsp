<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<link rel="stylesheet" type="text/css" href="/css/figure_gallery.css">
<script type="text/javascript" src="/javascript/imagebox.js"></script>

<c:if test="${!empty criteria.imageResults}">
  <div id="xpresimg_all">
    <input type="hidden" name="xpatsel_thumbnail_page" id="xpatsel_thumbnail_page_hidden_field" value="1"/>
    <div id="xpresimg_control_box">
      <span id="xpresimg_thumbs_title">Figure Gallery</span>
      <span id="xpresimg_controls"></span>
    </div>
    <div id="xpresimg_box"></div>
    <div id="imagebox_maxnote" style="display: none;"></div>
    <div id="xpresimg_imagePreload"></div>
  </div>

  <script type="text/javascript">
      var imageBox = new ImageBox();
      imageBox.setImageDivById("xpresimg_box");
      imageBox.setControlDivById("xpresimg_controls");
      imageBox.setHiddenCountFieldById("xpatsel_thumbnail_page_hidden_field");
      imageBox.setMaxImages(5000);
      imageBox.images = [
          <c:forEach items="${criteria.imageResults}" var="image">
          {
              imgThumb: "${image.imageThumbnail}",
              imgZdbId: "${image.imageZdbId}"
          },
          </c:forEach>
      ];
      document.getElementById('xpresimg_thumbs_title').innerHTML = "Figure Gallery (${fn:length(criteria.imageResults)} images)";
      function loadImages() {
          storedpage = imageBox.getHiddenCountInput().value;
          if ((storedpage != null) && (storedpage != "") && Number.isInteger(storedpage)) {
              imageBox.jumpToPage(storedpage);
          } else {
              imageBox.displayFirstSet();
          }
      }
      document.hasImages = true;
      loadImages();
  </script>
</c:if>
<br>
<c:choose>
  <c:when test="${!empty criteria.figureResults}">
    <zfin-expression-search:figure-results criteria="${criteria}"/>
  </c:when>
  <c:when test="${empty criteria.figureResults && !empty criteria.geneResults}">
    <zfin-expression-search:gene-results criteria="${criteria}"/>
  </c:when>
  <c:otherwise>
    <div class="no-results-found-message">
      No gene expression patterns were found for your search.
    </div>
  </c:otherwise>
</c:choose>

<zfin-expression-search:search-form criteria="${criteria}" title="Modify your search"/>
