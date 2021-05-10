<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="image" type="org.zfin.expression.Image"%>
<zfin2:subsection>

     <table class="summary">
     <tr>
     <th>Developmental Stage:</th>
     </tr>
     <td>

<c:if test="${!empty image.imageStage.start && image.imageStage.start ne image.imageStage.end}">
     <zfin:link entity="${image.imageStage.start}"/> <b> to </b><zfin:link entity="${image.imageStage.end}"/>
</c:if>
          <c:if test="${image.imageStage.start eq image.imageStage.end}">
               <zfin:link entity="${image.imageStage.start}"/>
          </c:if>
     </td>
     </table>




</zfin2:subsection>