<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Symbol">
        <zfin:abbrev entity="${formBean.marker}"/>
        <a class="small" href="/action/nomenclature/history/${formBean.marker.zdbID}">Nomenclature History</a>
    </z:attributeListItem>

    <z:attributeListItem label="Previous Names">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
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

    <z:attributeListItem>
                    <jsp:attribute name="label">
                        Description <a class='popup-link info-popup-link'
                                       href='/action/marker/note/automated-gene-desc'></a>
                    </jsp:attribute>
        <jsp:body>
            ${formBean.allianceGeneDesc.gdDesc}
        </jsp:body>
    </z:attributeListItem>
    <z:attributeListItem label="Genome Resources">
        <ul class="comma-separated">
            <c:forEach var="link" items="${formBean.otherMarkerPages}" varStatus="loop">
                <li><a href="${link.link}">${link.displayName}</a>
                        ${link.attributionLink}</li>
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