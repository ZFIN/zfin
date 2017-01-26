<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<table class="searchresults">
  <tr>
    <th>Publication</th>
    <th>Data</th>
    <th>Fish</th>
    <th>Stage Range</th>
    <th>Anatomy</th>
  </tr>
  <c:forEach items="${results}" var="result" varStatus="loop">
    <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="publication.zdbID">
      <td>
        <zfin:groupByDisplay loopName="loop" groupBeanCollection="${results}" groupByBean="publication.zdbID">
          <zfin:link entity="${result.publication}"/></td>
        </zfin:groupByDisplay>
      <td>
          <zfin:link entity="${result.figure}"/>
      </td>
      <td><zfin:link entity="${result.fish}"/></td>
      <td></td>
      <td></td>
    </zfin:alternating-tr>
  </c:forEach>

</table>