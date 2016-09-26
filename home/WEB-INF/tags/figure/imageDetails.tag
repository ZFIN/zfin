<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="image" type="org.zfin.expression.Image"%>
<zfin2:subsection>

     <table class="summary">
     <tr>
     <th>Figure Data:</th>
     </tr>
          <tr>
     <td>
          <c:if test="${!empty expressionSummaryMap[image.figure].startStage}">
               <div style="margin-top: 1em;">
                    <a href="/${image.figure.zdbID}#expDetail">Expression / Labeling details</a>
               </div>
          </c:if>
     </td>
          </tr>
          <tr>

      <td>
           <c:if test="${!empty phenotypeSummaryMap[image.figure].fish}">
                <div style="margin-top: 1em;">
                     <a href="/${image.figure.zdbID}#phenoDetail">Phenotype details</a>
                </div>
           </c:if>


      </td>
          </tr>
          <tr>
          <td>

               <zfin-figure:constructLinks figure="${image.figure}"/>
          </td>
          </tr>
          <tr>
               <td>

                    <zfin-figure:termLinks image="${image}"/>
               </td>
          </tr>
     </table>




</zfin2:subsection>