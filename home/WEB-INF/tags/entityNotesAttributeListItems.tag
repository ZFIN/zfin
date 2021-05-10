<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="entity" type="org.zfin.infrastructure.EntityNotes" rtexprvalue="true" required="true" %>
<%@ attribute name="additionalNote" required="false" %>

<authz:authorize access="hasRole('root')">
    <z:attributeListItem label="Curator Notes">
        <z:ifHasData test="${!empty entity.sortedDataNotes}" noDataMessage="None">
            <c:forEach var="curatorNote" items="${entity.sortedDataNotes}" varStatus="loop">
                <div class="${loop.last ? '' : 'mb-2'}">
                    <div>
                        <b class="mr-1">${curatorNote.curator.shortName}</b> ${curatorNote.date}
                    </div>
                    <zfin2:toggleTextLength
                            text="${curatorNote.note}"
                            idName="${zfn:generateRandomDomID()}"
                            shortLength="80"
                    />
                </div>
            </c:forEach>
        </z:ifHasData>
    </z:attributeListItem>
</authz:authorize>

<z:attributeListItem label="Note">
    <z:ifHasData test="${!empty entity.publicComments or !empty additionalNote}" noDataMessage="None">
        <div class="keep-breaks">${entity.publicComments}</div>
        ${additionalNote}
    </z:ifHasData>
</z:attributeListItem>
