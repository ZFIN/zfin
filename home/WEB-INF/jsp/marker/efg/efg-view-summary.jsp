<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        ${formBean.marker.name}
    </z:attributeListItem>

    <z:attributeListItem label="Symbol">
        ${formBean.marker.abbreviation}
    </z:attributeListItem>

    <z:attributeListItem label="Previous Names">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${formBean.previousNames}">
                <li>${markerAlias.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Type">
        <zfin2:externalLink
                href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">${formBean.zfinSoTerm.termName}</zfin2:externalLink>
    </z:attributeListItem>

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <authz:authorize access="hasRole('root')">
        <z:attributeListItem label="Curator Notes">
            <z:ifHasData test="${!empty formBean.marker.dataNotes}" noDataMessage="None">
                <c:forEach var="curatorNote" items="${formBean.marker.sortedDataNotes}"
                           varStatus="loopCurNote">
                    ${curatorNote.curator.shortName}&nbsp;&nbsp;${curatorNote.date}<br/>
                    <zfin2:toggleTextLength text="${curatorNote.note}" idName="${zfn:generateRandomDomID()}"
                                            shortLength="80"/>
                    ${!loopCurNote.last ? "<br/>&nbsp;<br>" : ""}
                </c:forEach>
            </z:ifHasData>
        </z:attributeListItem>
    </authz:authorize>

    <z:attributeListItem label="Note">
        <z:ifHasData test="${!empty formBean.marker.publicComments}" noDataMessage="None">
            <div class="keep-breaks">${formBean.marker.publicComments}</div>
        </z:ifHasData>
    </z:attributeListItem>
</z:attributeList>