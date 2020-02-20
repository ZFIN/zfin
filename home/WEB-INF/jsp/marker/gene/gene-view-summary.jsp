<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-prototype:attributeList>
    <zfin-prototype:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </zfin-prototype:attributeListItem>

    <zfin-prototype:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
    </zfin-prototype:attributeListItem>

    <zfin-prototype:attributeListItem label="Symbol">
        <zfin:abbrev entity="${formBean.marker}"/>
        <a class="small" href="/action/nomenclature/history/${formBean.marker.zdbID}">Nomenclature History</a>
    </zfin-prototype:attributeListItem>

    <zfin-prototype:attributeListItem label="Previous Names">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                <li>${markerAlias.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </zfin-prototype:attributeListItem>

    <zfin-prototype:attributeListItem label="Type">
        <zfin2:externalLink
                href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">${formBean.zfinSoTerm.termName}</zfin2:externalLink>
    </zfin-prototype:attributeListItem>

    <zfin-prototype:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </zfin-prototype:attributeListItem>

    <zfin-prototype:attributeListItem>
                    <jsp:attribute name="label">
                        Description <a class='popup-link info-popup-link'
                                       href='/action/marker/note/automated-gene-desc'></a>
                    </jsp:attribute>
        <jsp:body>
            ${formBean.allianceGeneDesc.gdDesc}
        </jsp:body>
    </zfin-prototype:attributeListItem>
    <zfin-prototype:attributeListItem label="Genome Resources">
        <ul class="comma-separated">
            <c:forEach var="link" items="${formBean.otherMarkerPages}" varStatus="loop">
                <li><a href="${link.link}">${link.displayName}</a>
                        ${link.attributionLink}</li>
            </c:forEach>
        </ul>
    </zfin-prototype:attributeListItem>

    <authz:authorize access="hasRole('root')">
        <zfin-prototype:attributeListItem label="Curator Notes">
            <zfin-prototype:ifHasData test="${!empty formBean.marker.dataNotes}" noDataMessage="None">
                <c:forEach var="curatorNote" items="${formBean.marker.sortedDataNotes}" varStatus="loopCurNote">
                    ${curatorNote.curator.shortName}&nbsp;&nbsp;${curatorNote.date}<br/>
                    <zfin2:toggleTextLength text="${curatorNote.note}" idName="${zfn:generateRandomDomID()}"
                                            shortLength="80"/>
                    ${!loopCurNote.last ? "<br/>&nbsp;<br>" : ""}
                </c:forEach>
            </zfin-prototype:ifHasData>
        </zfin-prototype:attributeListItem>
    </authz:authorize>

    <zfin-prototype:attributeListItem label="Note">
        <zfin-prototype:ifHasData test="${!empty formBean.marker.publicComments}" noDataMessage="None">
            <div class="keep-breaks">${formBean.marker.publicComments}</div>
        </zfin-prototype:ifHasData>
    </zfin-prototype:attributeListItem>
</zfin-prototype:attributeList>