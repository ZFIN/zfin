<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hasNotes" type="org.zfin.infrastructure.EntityNotes" rtexprvalue="true" required="true" %>


<authz:authorize ifAnyGranted="root">
    <c:if test="${!empty hasNotes.dataNotes}">
        <tr>
            <th valign="top">Curator Note:</th>
            <td>
                <c:forEach var="curatorNote" items="${hasNotes.dataNotes}" varStatus="loopCurNote">
                    ${curatorNote.curator.name}&nbsp;&nbsp;${curatorNote.date}<br/>
                    <zfin2:toggleTextLength text="${curatorNote.note}" idName="${loopCurNote.index}" shortLength="80"/>
                    ${!loopCurNote.last ? "<br/>&nbsp;<br>" : ""}
                </c:forEach>
            </td>
        </tr>
    </c:if>
</authz:authorize>

<c:if test="${!(empty hasNotes.publicComments)}">
    <tr>
        <th>Note:</th>
        <td>${zfn:escapeHtml(hasNotes.publicComments, false)}</td>
    </tr>
</c:if>

