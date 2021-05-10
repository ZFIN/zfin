<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="image" type="org.zfin.expression.Image"%>
<zfin2:subsection>

     <table class="summary">
     <tr>

     <td><b>Preparation</b></td>
          <td><b>Image Form</b></td>
          <td><b>View</b></td>
          <td><b>Direction</b></td>
          </tr>
          <tr>
               <td>${image.preparation}</td>
               <td>${image.form}</td>
               <td>${image.view}</td>
               <td>${image.direction}</td>
          </tr>

     </table>




</zfin2:subsection>