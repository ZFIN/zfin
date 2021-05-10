<!-- called by candidate_view.jsp -->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
    <body>
        <table class="searchresults">
        <tr style="background: #ccc"><th></th><th>Accession</th><th>Length</th><th>Score</th><th>Positives</th><th>Expect</th><th>ZFIN</th>
          <c:forEach var="hit" items="${formBean.hits}" varStatus="loop">

            <c:choose>
                <c:when test="${loop.count % 2 == 0}"> <tr class="odd"> </c:when>
                <c:otherwise> <tr> </c:otherwise>
            </c:choose>

            <td><a href="">${hit.name}</a></td>

            <td>${hit.accession}</td>

            <td>${hit.accession.length}</td>

           <td>${hit.score}</td>

           <td>${hit.positivesNum}</td>

           <td>${hit.expectValue}</td>


          </tr>
        </c:forEach>
      </table>
  </body>
</html>

