<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<div class="d-flex">
    <div class="data-page-nav-container">
        <ul class="nav nav-pills flex-column">
            <li class="nav-item" role="presentation"><a class="nav-link" href="#summary">Summary</a></li>
            <li class="nav-item" role="presentation"><a class="nav-link" href="#antibodies">Antibodies</a></li>
        </ul>
    </div>

    <div class="flex-grow-1 p-4">
        <div id="summary">
            <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
            <h1>${formBean.marker.abbreviation}</h1>

            <dl class="row">
                <dt class="col-sm-2">ID</dt>
                <dd class="col-sm-10">${formBean.marker.zdbID}</dd>

                <dt class="col-sm-2">Name</dt>
                <dd class="col-sm-10"><zfin:name entity="${formBean.marker}"/></dd>

                <dt class="col-sm-2">Symbol</dt>
                <dd class="col-sm-10">
                    <zfin:abbrev entity="${formBean.marker}"/>
                    <a class="small" href="/action/nomenclature/history/${formBean.marker.zdbID}">Nomenclature History</a>
                </dd>

                <dt class="col-sm-2">Previous Names</dt>
                <dd class="col-sm-10">
                    <ul class="comma-separated">
                        <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                            <li>${markerAlias.linkWithAttribution}</li>
                        </c:forEach>
                    </ul>
                </dd>

                <dt class="col-sm-2">Type</dt>
                <dd class="col-sm-10">
                    <zfin2:externalLink href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">${formBean.zfinSoTerm.termName}</zfin2:externalLink>
                </dd>

                <dt class="col-sm-2">Location</dt>
                <dd class="col-sm-10">
                    <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
                </dd>

                <dt class="col-sm-2">Description <a class='popup-link info-popup-link' href='/action/marker/note/automated-gene-desc'></a></dt>
                <dd class="col-sm-10">
                    ${formBean.allianceGeneDesc.gdDesc}
                </dd>

                <authz:authorize access="hasRole('root')">
                    <dt class="col-sm-2">Curator Notes</dt>
                    <dd class="col-sm-10">
                        <c:choose>
                            <c:when test="${!empty formBean.marker.dataNotes}">
                                <c:forEach var="curatorNote" items="${formBean.marker.sortedDataNotes}" varStatus="loopCurNote">
                                    ${curatorNote.curator.shortName}&nbsp;&nbsp;${curatorNote.date}<br/>
                                    <zfin2:toggleTextLength text="${curatorNote.note}" idName="${zfn:generateRandomDomID()}" shortLength="80"/>
                                    ${!loopCurNote.last ? "<br/>&nbsp;<br>" : ""}
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <i class="text-muted">None</i>
                            </c:otherwise>
                        </c:choose>
                    </dd>
                </authz:authorize>

                <dt class="col-sm-2">Note</dt>
                <dd class="col-sm-10">
                    <c:choose>
                        <c:when test="${!(empty formBean.marker.publicComments)}">
                            <div class="keep-breaks">${formBean.marker.publicComments}</div>
                        </c:when>
                        <c:otherwise>
                            <i class="text-muted">None</i>
                        </c:otherwise>
                    </c:choose>
                </dd>
            </dl>
        </div>

        <div class="section" id="antibodies">
            <div class="heading">Antibodies</div>
            <table class="table table-hover">
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
            </table>
        </div>
    </div>
</div>