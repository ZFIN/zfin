<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ANTIBODIES" value="Antibodies"/>
<c:set var="PLASMIDS" value="Plasmids"/>
<c:set var="PATHWAYS" value="Interactions and Pathways"/>
<c:set var="MUTANTS" value="Mutations"/>
<c:set var="DISEASES" value="Diseases"/>
<c:set var="PROTEINS" value="Proteins"/>
<c:set var="CONSTRUCTS" value="Constructs"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>

<zfin-prototype:dataPage
        sections="${[SUMMARY, MUTANTS, DISEASES, PROTEINS, PATHWAYS, ANTIBODIES, PLASMIDS, CONSTRUCTS, MARKERRELATIONSHIPS, SEQUENCES]}">
    <zfin-prototype:dataManagerDropdown>
        <a class="dropdown-item active" href="/action/marker/gene/prototype-view/${formBean.marker.zdbID}">View</a>
        <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
    </zfin-prototype:dataManagerDropdown>

    <div id="${zfn:makeDomIdentifier(SUMMARY)}">
        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

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
    </div>

    <zfin-prototype:section title="${MUTANTS}">
        <zfin-prototype:section title="Mutants">
            <zfin-prototype:dataTable collapse="true" hasData="${!empty formBean.mutantOnMarkerBeans.features}">
                <thead>
                <tr>
                    <th width="10%">Allele</th>
                    <th width="13%">Type</th>
                    <th width="15%">Localization</th>
                    <th width="20%">Consequence</th>
                    <th width="10%">Mutagen</th>
                    <th width="50%">Suppliers</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="feature" items="${formBean.mutantOnMarkerBeans.features}" varStatus="loop">
                    <tr>
                        <td>
                            <a href="/${feature.zdbID}">${feature.abbreviation}</a>
                        </td>
                        <td>
                                ${feature.type.display}
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${!empty feature.geneLocalizationStatement}">
                                    ${feature.geneLocalizationStatement}
                                </c:when>
                                <c:otherwise>
                                    <span class="no-data-tag">Unknown</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${!empty feature.transcriptConsequenceStatement}">
                                    ${feature.transcriptConsequenceStatement}
                                </c:when>
                                <c:otherwise>
                                    <span class="no-data-tag">Unknown</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:set var="mutagen" value="${feature.featureAssay.mutagen}"/>
                            <c:if test="${mutagen ne zfn:getMutagen('not specified')}">
                                ${feature.featureAssay.mutagen.toString()}
                            </c:if>
                        </td>
                        <td>
                            <ul class="list-unstyled">
                                <c:forEach var="supplier" items="${feature.suppliers}">
                                    <li><a href="/${supplier.organization.zdbID}"> ${supplier.organization.name}</a>
                                        <c:if test="${!empty supplier.orderURL}">
                                            <a href="${supplier.orderURL}"> (order this)</a>
                                        </c:if>
                                    </li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </zfin-prototype:dataTable>
        </zfin-prototype:section>

        <zfin-prototype:section title="Sequence Targeting Reagents">
            <zfin-prototype:dataTable collapse="true"
                                      hasData="${!empty formBean.mutantOnMarkerBeans.knockdownReagents}">
                <thead>
                <tr>
                    <th>Targeting Reagent</th>
                    <th>Created Alleles</th>
                    <th>Publications</th>
                </tr>
                </thead>

                <c:forEach items="${formBean.mutantOnMarkerBeans.knockdownReagents}" var="bean" varStatus="loop">
                    <tr>
                        <td><zfin:link entity="${bean.marker}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${bean.marker.type == 'MRPHLNO'}">
                                    <i class="no-data-tag">N/A</i>
                                </c:when>
                                <c:otherwise>
                                    <ul class="comma-separated">
                                        <c:forEach items="${bean.genomicFeatures}" var="feature">
                                            <li><zfin:link entity="${feature}"/></li>
                                        </c:forEach>
                                    </ul>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <a href="/action/marker/citation-list/${bean.marker.zdbID}">${fn:length(bean.marker.publications)}</a>
                        </td>
                    </tr>
                </c:forEach>
            </zfin-prototype:dataTable>
        </zfin-prototype:section>
    </zfin-prototype:section>

    <zfin-prototype:section title="${DISEASES}">
        <zfin-prototype:section title="Associated with <i>${formBean.marker.abbreviation}</i> human ortholog">
            <zfin-prototype:dataTable collapse="true" hasData="${!empty formBean.diseaseDisplays}">
                <thead>
                <tr>
                    <th width="25%">Disease Ontology Term</th>
                    <th width="20%">Multi-Species Data</th>
                    <th width="25%">OMIM Term</th>
                    <th width="20%">OMIM Phenotype ID</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="row" items="${formBean.diseaseDisplays}" varStatus="loop">
                    <tr>
                        <zfin-prototype:groupedCell items="${formBean.diseaseDisplays}" loop="${loop}"
                                                    property="diseaseTerm">
                            <c:if test="${!empty row.diseaseTerm}">
                                <zfin:link entity="${row.diseaseTerm}" longVersion="true"/>
                            </c:if>
                        </zfin-prototype:groupedCell>

                        <zfin-prototype:groupedCell items="${formBean.diseaseDisplays}" loop="${loop}"
                                                    property="diseaseTerm">
                            <c:if test="${!empty row.diseaseTerm}">
                                <a href="http://www.alliancegenome.org/disease/${row.diseaseTerm.oboID}">Alliance</a>
                            </c:if>
                        </zfin-prototype:groupedCell>

                        <td>${row.omimPhenotype.name}</td>

                        <td>
                            <c:if test="${!empty row.omimPhenotype.omimNum}">
                                <a href="http://omim.org/entry/${row.omimPhenotype.omimNum}">${row.omimPhenotype.omimNum}</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </zfin-prototype:dataTable>
        </zfin-prototype:section>
        <zfin-prototype:section title="Associated with <i>${formBean.marker.abbreviation}</i> via experimental Models">
            <zfin-prototype:dataTable collapse="true" hasData="${!empty formBean.diseaseModelDisplays}">
                <thead>
                <tr>
                    <th>Human Disease</th>
                    <th>Fish</th>
                    <th>Conditions</th>
                    <th>Citations</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${formBean.diseaseModelDisplays}" var="disease" varStatus="loop">
                    <tr>
                        <zfin-prototype:groupedCell items="${formBean.diseaseModelDisplays}" loop="${loop}"
                                                    property="disease">
                            <zfin:link entity="${disease.disease}"/>
                        </zfin-prototype:groupedCell>
                        <td><zfin:link entity="${disease.experiment.fish}"/></td>
                        <td><zfin:link entity="${disease.experiment.experiment}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${fn:length(disease.publications) == 1}">
                                    <zfin:link entity="${disease.publications[0]}"/>
                                </c:when>
                                <c:otherwise>
                                    <a href="/action/ontology/fish-model-publication-list/${disease.disease.oboID}/${disease.experiment.fish.zdbID}">
                                        (${fn:length(disease.publications)})
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </zfin-prototype:dataTable>
        </zfin-prototype:section>
    </zfin-prototype:section>

    <zfin-prototype:section title="${PROTEINS}">
        <zfin-prototype:dataTable collapse="true" hasData="${!empty formBean.proteinDomainBeans}">
            <c:if test="${!fn:contains(formBean.marker.zdbID,'RNAG')}">
                <thead>
                <tr>
                    <th style="width: 17%">Type</th>
                    <th style="width: 17%">InterPro ID</th>
                    <th style="width: 17%">Name</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="category" items="${formBean.proteinDomainBeans}">
                    <tr>
                        <td>${category.ipType}</td>
                        <td><a href="http://www.ebi.ac.uk/interpro/entry/${category.ipID}">${category.ipID}</a></td>
                        <td>${category.ipName}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </c:if>
        </zfin-prototype:dataTable>
    </zfin-prototype:section>

    <zfin-prototype:section title="${PATHWAYS}">
        <zfin-prototype:dataTable hasData="${!empty formBean.pathwayDBLinks}">
            <c:forEach var="link" items="${formBean.pathwayDBLinks}" varStatus="loop">
                <tr>
                    <td><a href="${link.link}">${link.referenceDatabaseName}</a></td>
                </tr>
            </c:forEach>
        </zfin-prototype:dataTable>
    </zfin-prototype:section>

    <zfin-prototype:section title="${ANTIBODIES}">
        <zfin-prototype:dataTable collapse="true" hasData="${!empty formBean.antibodyBeans}">
            <thead>
            <tr>
                <th style="width: 17%">Name</th>
                <th style="width: 17%">Type</th>
                <th style="width: 10%">Isotype</th>
                <th style="width: 17%">Host Organism</th>
                <th style="width: 17%">Assay <a class="popup-link info-popup-link"
                                                href="/ZFIN/help_files/antibody_assay_help.html"></a></th>
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

    <zfin-prototype:section title="${PLASMIDS}">
        <zfin-prototype:dataTable hasData="${!empty formBean.plasmidDBLinks}">
            <c:forEach var="link" items="${formBean.plasmidDBLinks}" varStatus="loop">
                <tr>
                    <td><a href="${link.link}">${link.referenceDatabaseName}:${link.accNumDisplay}</a></td>
                </tr>
            </c:forEach>
        </zfin-prototype:dataTable>
    </zfin-prototype:section>

    <zfin-prototype:section title="${CONSTRUCTS}">
        <div class="__react-root" id="GeneConstructsTable" data-gene-id="${formBean.marker.zdbID}"></div>
    </zfin-prototype:section>

    <zfin-prototype:section title="${MARKERRELATIONSHIPS}">
        <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
    </zfin-prototype:section>

    <zfin-prototype:section title="${SEQUENCES}">
        <div class="__react-root" id="GeneSequencesTable" data-gene-id="${formBean.marker.zdbID}"></div>
    </zfin-prototype:section>

</zfin-prototype:dataPage>

<script src="${zfn:getAssetPath("react.js")}"></script>
