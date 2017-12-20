<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="entity" type="org.zfin.infrastructure.EntityNotes" rtexprvalue="true" required="true" %>


<authz:authorize access="hasRole('root')">
    <c:if test="${!empty entity.dataNotes}">
        <tr>
            <th valign="top">Curator Note:</th>
            <td>
                <c:forEach var="curatorNote" items="${entity.sortedDataNotes}" varStatus="loopCurNote">
                    ${curatorNote.curator.shortName}&nbsp;&nbsp;${curatorNote.date}<br/>
                    <zfin2:toggleTextLength text="${curatorNote.note}" idName="${zfn:generateRandomDomID()}" shortLength="80"/>
                    ${!loopCurNote.last ? "<br/>&nbsp;<br>" : ""}
                </c:forEach>
            </td>
        </tr>
    </c:if>
</authz:authorize>

<c:if test="${!(empty entity.publicComments)}">
    <tr>
        <th>Note:</th>
        <td class="keep-breaks">${entity.publicComments}</td>
    </tr>
</c:if>

