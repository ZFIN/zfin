<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary" />
<c:set var="ANTIBODIES" value="Antibodies" />

<zfin-prototype:dataPage sections="${[SUMMARY, ANTIBODIES]}">
    <div id="${zfn:makeDomIdentifier(SUMMARY)}">
        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1>${formBean.marker.abbreviation}</h1>

        <zfin-prototype:attributeList>
            <zfin-prototype:attributeListItem label="ID">
                ${formBean.marker.zdbID}
            </zfin-prototype:attributeListItem>

            <zfin-prototype:attributeListItem label="Name">
                <zfin:name entity="${formBean.marker}" />
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
                <zfin2:externalLink href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">${formBean.zfinSoTerm.termName}</zfin2:externalLink>
            </zfin-prototype:attributeListItem>

            <zfin-prototype:attributeListItem label="Location">
                <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
            </zfin-prototype:attributeListItem>

            <zfin-prototype:attributeListItem>
                    <jsp:attribute name="label">
                        Description <a class='popup-link info-popup-link' href='/action/marker/note/automated-gene-desc'></a>
                    </jsp:attribute>
                <jsp:body>
                    ${formBean.allianceGeneDesc.gdDesc}
                </jsp:body>
            </zfin-prototype:attributeListItem>

            <authz:authorize access="hasRole('root')">
                <zfin-prototype:attributeListItem label="Curator Notes">
                    <zfin-prototype:ifHasData test="${!empty formBean.marker.dataNotes}" noDataMessage="None">
                        <c:forEach var="curatorNote" items="${formBean.marker.sortedDataNotes}" varStatus="loopCurNote">
                            ${curatorNote.curator.shortName}&nbsp;&nbsp;${curatorNote.date}<br/>
                            <zfin2:toggleTextLength text="${curatorNote.note}" idName="${zfn:generateRandomDomID()}" shortLength="80"/>
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
    </div>

    <zfin-prototype:section title="${ANTIBODIES}">
        <zfin-prototype:dataTable hasData="${!empty formBean.antibodyBeans}">
            <thead>
                <tr>
                    <th style="width: 17%">Name</th>
                    <th style="width: 17%">Type</th>
                    <th style="width: 10%">Isotype</th>
                    <th style="width: 17%">Host Organism</th>
                    <th style="width: 17%">Assay <a class="popup-link info-popup-link" href="/ZFIN/help_files/antibody_assay_help.html"></a></th>
                    <th style="width: 17%">Source</th>
                    <th style="width: 5%">Publications</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="antibodyBean" items="${formBean.antibodyBeans}">
                    <c:set var="antibody" value="${antibodyBean.antibody}"/>
                    <tr>
                        <td><zfin:link entity="${antibody}"/></td>
                        <td>${antibody.clonalType}</td>
                        <td>
                                ${antibody.heavyChainIsotype}
                            <c:if test="${antibody.heavyChainIsotype != null && antibody.lightChainIsotype != null}">, </c:if>
                                ${antibody.lightChainIsotype}
                        </td>
                        <td>
                                ${antibody.hostSpecies}
                        </td>
                        <td>
                            <ul class="comma-separated">
                                <c:forEach var="assay" items="${antibody.distinctAssayNames}">
                                    <li>${assay}</li>
                                </c:forEach>
                            </ul>
                        </td>
                        <td>
                            <zfin2:orderThis markerSuppliers="${antibody.suppliers}"
                                             accessionNumber="${antibody.zdbID}"
                                             organization=""/>
                        </td>
                        <td class="text-right">
                            <a href="/action/antibody/antibody-publication-list?antibodyID=${antibodyBean.antibody.zdbID}&orderBy=author">${antibodyBean.numPubs}</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </zfin-prototype:dataTable>
    </zfin-prototype:section>
</zfin-prototype:dataPage>


