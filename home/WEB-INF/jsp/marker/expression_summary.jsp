<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

  <h3 style="background: #ccc;">Expression Summary</h3>
  <b>Gene Name: <span class="genedom">${formBean.gene.name}</span> </b><br>
  <!-- TODO: gene symbol should be a link -->
  <b>Gene Symbol: <span class="genedom">${formBean.gene.symbol}</span> </b><br><br>

  <!-- TODO: we need a css class specifically for detail tables like this -->
  <table class="searchresults">
    <tr style="background: #ccc"><th>Stage</th><th>Anatomy</th><th>Figures</th></tr>

      <c:forEach var="xsa" items="${formBean.xsaList}" varStatus="loop">

        <c:choose>
            <c:when test="${loop.count % 2 == 0}"> <tr class="odd"> </c:when>
            <c:otherwise> <tr> </c:otherwise>
        </c:choose>

        <td>
           <a href="/zf_info/zfbook/stages/index.html">
             ${xsa.stage.name}
           </a>
        </td>

        <td>
            <c:forEach var="anat" items="${xsa.anatomyTerms}" varStatus="loop">
               <a href="/action/anatomy/term-detail?anatomyItem.zdbID=${anat.zdbID}">${anat.name}</a><c:if test="${!loop.last}">,</c:if>
            </c:forEach>
        </td>

        <td>${xsa.figureCount}</td>

      </tr>

      </c:forEach>
 </table>
