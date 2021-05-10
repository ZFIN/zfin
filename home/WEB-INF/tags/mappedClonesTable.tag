<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="mappedClones" required="true" type="java.util.Collection" %>
<%@ attribute name="marker" required="true" type="org.zfin.marker.Marker" %>

<c:if test="${mappedClones.size() > 0}">
  <table class="summary rowstripes">
    <tr>
      <th colspan="2">
        Mapped Clones containing <zfin:abbrev entity="${marker}"/>
      </th>
    </tr>
    <c:forEach var="clone" items="${mappedClones}">
      <tr>
        <td width="10%">
          <zfin:link entity="${clone}"/>
        </td>
        <td>
          <zfin2:displayLocation entity="${clone}"/>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>