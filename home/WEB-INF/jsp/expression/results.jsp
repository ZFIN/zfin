<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<z:page>
  <div class="__react-root" id="ExpressionImageGallery__0"
       data-query="${fn:escapeXml(pageContext.request.queryString)}"></div>
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
  <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>