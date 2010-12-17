<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="numColumns" value="5"/>

<table style="background: lightgray;" cellpadding="5">
<c:forEach var="feature" items="${features}" varStatus="loop">
  ${(loop.index%numColumns ==0  ?  "<tr/>" : "")}
  <td>
  <zfin:link entity="${feature}"/>
  </td>
  ${(loop.index+1%numColumns ==0 or loop.last  ?  "</tr>" : "")}
</c:forEach>
</table>


