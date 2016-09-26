<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="image" type="org.zfin.expression.Image"%>
<zfin2:subsection>

     <table class="summary">
     <tr>
     <th>Developmental Stage:</th>
     </tr>
     <td>
<c:if test="${!empty image.imageStage.start}">
     ${image.imageStage.start.nameLong} <b> to </b>${image.imageStage.end.nameLong}
</c:if>
     </td>
     </table>




</zfin2:subsection>