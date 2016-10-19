<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="entity" type="org.zfin.infrastructure.EntityNotes" rtexprvalue="true" required="true" %>


<authz:authorize access="hasRole('root')">
   <tr ng-if="editMode" curator-notes marker-id="${entity.zdbID}" edit="1">
   <tr ng-if="!editMode" curator-notes marker-id="${entity.zdbID}" edit="0">
   </tr>
</authz:authorize>

<c:if test="${!(empty entity.publicComments)}">
    <tr>
        <th>Note:</th>
        <td>${entity.publicComments}</td>
    </tr>
</c:if>

