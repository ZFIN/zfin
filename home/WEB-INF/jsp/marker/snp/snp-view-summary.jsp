
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


    <z:attributeListItem label="Variant Allele">
        ${formBean.variant}
    </z:attributeListItem>
    <z:attributeListItem label="Sequence">
        <div>${formBean.sequence.startToOffset}</div>
        <span style="color: green;">
                ${formBean.sequence.ambiguity}
        </span>
        <div>${formBean.sequence.offsetToEnd}</div>
        <div>${formBean.sequence.ambiguity} = ${formBean.variant}</div>
        <div class='btn-group'>
            <button
                    class='btn btn-outline-secondary btn-sm dropdown-toggle'
                    data-toggle='dropdown'
                    aria-haspopup='true'
                    aria-expanded='false'
            >
                Select Tool
            </button>
            <div class='dropdown-menu'>

                <a class='dropdown-item' href="${formBean.ncbiBlastUrl}${formBean.sequence.sequence}">NCBI
                    BLAST</a>
                <a class='dropdown-item'
                   href="/action/blast/blast?&program=blastn&sequenceType=nt&queryType=FASTA&shortAndNearlyExact=true&expectValue=1e-10&dataLibraryString=RNASequences&querySequence=${formBean.sequence.sequence}">ZFIN
                    BLAST</a>
                <br>
            </div>
        </div>
    </z:attributeListItem>

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
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

