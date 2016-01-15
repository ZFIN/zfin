<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="markerCollection" type="java.util.Collection"%>

<c:if test="${fn:length(markerCollection) > 0 }">
  <c:forEach var="tgConstruct" items="${markerCollection}" varStatus="constructLoop">
        <i><zfin:link entity="${tgConstruct}"/></i><c:if test="${!constructLoop.last}">,&nbsp</c:if>
  </c:forEach>
</c:if>
