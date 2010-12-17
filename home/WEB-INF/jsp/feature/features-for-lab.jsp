<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:forEach var="feature" items="${features}">
  <zfin:link entity="${feature}"/>
    <br/>
</c:forEach>


