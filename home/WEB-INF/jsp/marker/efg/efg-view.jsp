<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="CONSTRUCTS" value="Constructs"/>
<c:set var="EXPRESSION" value="Expression"/>
<c:set var="SEQUENCE" value="Sequence Information"/>


<z:dataPage
        sections="${[SUMMARY, EXPRESSION,  CONSTRUCTS, SEQUENCE]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>
    <jsp:body>
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
    </z:dataManagerDropdown>

    <div id="${zfn:makeDomIdentifier(SUMMARY)}">
        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

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
                    <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                        <li>${markerAlias.linkWithAttribution}</li>
                    </c:forEach>
                </ul>
            </z:attributeListItem>

            <z:attributeListItem label="Type">
                ${formBean.zfinSoTerm.oboID}
                <zfin2:externalLink
                        href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">${formBean.zfinSoTerm.termName}</zfin2:externalLink>
            </z:attributeListItem>

            <z:attributeListItem label="Location">
                <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
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

            <z:attributeListItem label="Citations">
                <a href="/action/marker/citation-list/${formBean.marker.zdbID}">(${formBean.numPubs})</a>
            </z:attributeListItem>

        </z:attributeList>

        <z:section title="${EXPRESSION}">
            <jsp:include page="efg-view-expression-header.jsp"/>

        </z:section>


        <z:section title="${CONSTRUCTS}">
            <div class="__react-root" id="GeneConstructsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>



        <z:section title="${SEQUENCE}">
            <div class="__react-root" id="GeneSequencesTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>



    </div>

    </jsp:body>
</z:dataPage>

<script src="${zfn:getAssetPath("react.js")}"></script>
