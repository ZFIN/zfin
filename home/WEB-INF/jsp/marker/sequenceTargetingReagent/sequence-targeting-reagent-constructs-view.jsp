<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:dataTable collapse="true"
             hasData="${!empty formBean.constructs}">
 <thead>
 <tr>
  <th>Construct</th>
 </tr>
 </thead>
 <tbody>
 <c:forEach var="construct" items="${formBean.constructs}" varStatus="loop">

  <td>
   <zfin:link entity="${construct}"/>
  </td>
 </c:forEach>
 </tbody>
</z:dataTable>


