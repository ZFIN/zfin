<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<c:choose>
  <c:when test="${!empty criteria.figureResults}">
    <zfin-expression-search:figure-results criteria="${criteria}"/>
  </c:when>
  <c:when test="${empty criteria.figureResults && !empty criteria.geneResults}">
    <zfin-expression-search:gene-results criteria="${criteria}"/>
  </c:when>
  <c:otherwise>
    <%-- no results...need to explicitly capture 'searchHappened'--%>
  </c:otherwise>
</c:choose>

<zfin-expression-search:search-form criteria="${criteria}"/>
