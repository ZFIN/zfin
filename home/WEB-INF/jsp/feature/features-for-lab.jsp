<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<br/>
<c:forEach var="feature" items="${features}" varStatus="loop">
  <zfin:link entity="${feature}"/>
    <br/>
</c:forEach>


