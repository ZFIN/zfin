<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
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

    <z:attributeListItem label="Regulatory Regions">
        <ul class="comma-separated">
            <c:forEach var="regulatoryRegion" items="${formBean.regulatoryRegionPresentations}">
                <li>${regulatoryRegion.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Coding Sequences">
        <ul class="comma-separated">
            <c:forEach var="codingSequence" items="${formBean.codingSequencePresentations}">
                <li>${codingSequence.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Contains Sequences">
        <ul class="comma-separated">
            <c:forEach var="containSequence" items="${formBean.containsSequencePresentations}">
                <li>${containSequence.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <authz:authorize access="hasRole('root')">
        <z:attributeListItem label="Curator Notes">
            <z:ifHasData test="${!empty formBean.marker.dataNotes}" noDataMessage="None">
                <c:forEach var="curatorNote" items="${formBean.marker.sortedDataNotes}" varStatus="loopCurNote">
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
