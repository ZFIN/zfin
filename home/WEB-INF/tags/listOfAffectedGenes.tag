<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="markerCollection" type="java.util.Collection"%>

<c:if test="${fn:length(markerCollection) > 0 }">
  <c:forEach var="affectedGene" items="${markerCollection}" varStatus="geneLoop">
        <zfin:link entity="${affectedGene}"/><c:if test="${!geneLoop.last}">,&nbsp</c:if>
  </c:forEach>
</c:if>
