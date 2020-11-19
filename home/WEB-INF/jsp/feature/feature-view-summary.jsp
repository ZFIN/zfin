<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.feature.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        ${formBean.feature.name}
    </z:attributeListItem>

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.synonyms}" name="Synonyms"/>

    <z:attributeListItem label="Affected Genomic Region">
        <c:choose>
            <c:when test="${fn:length(formBean.feature.affectedGenes) > 0 }">
                <ul class="comma-separated">
                    <c:forEach var="mRel" items="${formBean.feature.affectedGenesReln}" varStatus="loop">
                        <li><a href="/${mRel.marker.zdbID}"><i>${mRel.marker.abbreviation}</i></a>
                            <c:if test="${mRel.publicationCount > 0}">
                                <c:choose>
                                    <c:when test="${mRel.publicationCount == 1}">
                                        (<a href="/${mRel.singlePublication.zdbID}">${mRel.publicationCount}</a>)
                                    </c:when>
                                    <c:otherwise>
                                        (<a href="/action/infrastructure/data-citation-list/${mRel.zdbID}">${mRel.publicationCount}</a>)
                                    </c:otherwise>
                                </c:choose>
                            </c:if></li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <c:if test="${formBean.feature.type.transgenic}">
                    <c:if test="${!(formBean.feature.knownInsertionSite)}">
                        This feature is representative of one or more unknown insertion sites.
                    </c:if>
                </c:if>
            </c:otherwise>
        </c:choose>
    </z:attributeListItem>

    <z:attributeListItem label="Construct">
        <z:ifHasData test="${!empty formBean.sortedConstructRelationships}" noDataMessage="None">
            <ul class="comma-separated">
                <c:forEach var="mRel" items="${formBean.sortedConstructRelationships}">
                    <li><a href="/${mRel.marker.zdbID}"><i>${mRel.marker.name}</i></a>
                        <c:if test="${mRel.publicationCount > 0}">
                            <c:choose>
                                <c:when test="${mRel.publicationCount == 1}">
                                    (<a href="/${mRel.singlePublication.zdbID}">${mRel.publicationCount}</a>)
                                </c:when>
                                <c:otherwise>
                                    (<a href="/action/infrastructure/data-citation-list/${mRel.zdbID}">${mRel.publicationCount}</a>)
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </li>
                </c:forEach>
            </ul>
        </z:ifHasData>
    </z:attributeListItem>

    <z:attributeListItem label="Type">
        ${formBean.feature.displayType}
        <c:if test="${!empty formBean.featureTypeAttributions}">
            <c:choose>
                <c:when test="${fn:length(formBean.featureTypeAttributions) == 1 }">
                    (<a href="/${formBean.featureTypeAttributions[0].sourceZdbID}">1</a>)
                </c:when>
                <c:otherwise>
                    (<a href="/action/feature/type-citation-list/${formBean.feature.zdbID}">${fn:length(formBean.featureTypeAttributions)}</a>)
                </c:otherwise>
            </c:choose>
        </c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Protocol">
        <c:set var="mutagen" value="${formBean.feature.featureAssay.mutagen}"/>
        <c:set var="mutagee" value="${formBean.feature.featureAssay.mutagee}"/>
        <c:choose>
            <c:when test="${mutagen eq null || mutagen eq zfn:getMutagen('not specified')}">
            </c:when>
            <c:when test="${mutagee eq zfn:getMutagee('not specified') && mutagen eq zfn:getMutagen('not specified')}">
            </c:when>
            <c:when test="${mutagee eq zfn:getMutagee('not specified') && mutagen ne zfn:getMutagen('not specified')}">
                ${mutagen.toString()}&nbsp;
                <c:if test="${formBean.createdByRelationship ne null && fn:length(formBean.createdByRelationship) > 0}">
                    <ul class="comma-separated">
                        <c:forEach var="createdBy" items="${formBean.createdByRelationship}">
                            <li><zfin:link entity="${createdBy.marker}"/></li>
                        </c:forEach>
                    </ul>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${formBean.createdByRelationship ne null && fn:length(formBean.createdByRelationship) > 0}">
                        ${mutagee.toString()} treated with
                        <ul class="comma-separated">
                            <c:forEach var="createdBy" items="${formBean.createdByRelationship}">
                                <li><zfin:link entity="${createdBy.marker}"/></li>
                            </c:forEach>
                        </ul>
                    </c:when>
                    <c:otherwise>
                        ${mutagee.toString()} treated with ${mutagen.toString()}
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </z:attributeListItem>

    <z:attributeListItem label="Lab of Origin">
        <z:ifHasData test="${formBean.feature.sources ne null && fn:length(formBean.feature.sources) > 0}"
                     noDataMessage="None">
            <c:forEach var="source" items="${formBean.feature.sources}">
                <c:if test="${source.organization.zdbID != 'ZDB-LAB-000914-1'}">
                    <zfin:link entity="${source.organization}"/>
                </c:if>
            </c:forEach>
        </z:ifHasData>
    </z:attributeListItem>

    <z:attributeListItem label="Current Source">
        <c:choose>
            <c:when test="${formBean.feature.suppliers ne null && fn:length(formBean.feature.suppliers) > 0}">
                <c:forEach var="supplier" items="${formBean.feature.suppliers}" varStatus="status">
                    <a href="/${supplier.organization.zdbID}" id="${supplier.organization.zdbID}">
                            ${supplier.organization.name}
                    </a>
                    <c:if test="${supplier.zirc || supplier.ezrc || supplier.czrc}">&nbsp;
                        <zfin2:orderThis organization="${supplier.organization}"
                                         accessionNumber="${formBean.feature.zdbID}"/>
                    </c:if>
                    <c:if test="${supplier.moensLab}">&nbsp;(<a href="http://labs.fhcrc.org/moens/Tilling_Mutants/${formBean.feature.singleRelatedMarker.abbreviation}"><span style="font-size: smaller; ">request this mutant</span></a>)
                    </c:if>
                    <c:if test="${supplier.solnicaLab}">&nbsp;(<a href="http://devbio.wustl.edu/solnicakrezellab/${formBean.feature.singleRelatedMarker.abbreviation}.htm"><span style="font-size: smaller; ">request this mutant</span></a>)
                    </c:if>
                    <c:if test="${supplier.riken}">&nbsp;(<a href="http://www.shigen.nig.ac.jp/zebrafish/strainDetailAction.do?zfinId=${formBean.feature.singleRelatedGenotype.zdbID}"><span style="font-size: smaller; ">order this</span></a>)
                    </c:if>
                    <c:if test="${!status.last}"><br/></c:if>
                </c:forEach>
            </c:when>
            <c:when test="${!empty formBean.genotypeDisplays}">

                <c:forEach var="genotypeDisplay" items="${formBean.genotypeDisplays}" varStatus="loop">
                    <c:if test="${genotypeDisplay.genotype.extinct}">
                        <span style="font-size: small; color: red; ">extinct</span> <i class="warning-icon"
                                                                                       title="extinct"></i>
                    </c:if>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <span class="no-data-tag"></span>
            </c:otherwise>
        </c:choose>
    </z:attributeListItem>

    <z:attributeListItem label="Other Pages">
        <ul class="comma-separated">
            <c:forEach var="link" items="${formBean.summaryPageDbLinks}" varStatus="loop">
                <li>
                    <zfin:link entity="${link}"/>
                    <c:if test="${link.publicationCount > 0}">
                        <c:choose>
                            <c:when test="${link.publicationCount == 1}">
                                (<a href="/${link.singlePublication.zdbID}">${link.publicationCount}</a>)
                            </c:when>
                            <c:otherwise>
                                (<a href="/action/infrastructure/data-citation-list/${link.zdbID}">${link.publicationCount}</a>)
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </li>
            </c:forEach>
            <c:if test="${!empty formBean.aaLink}">
                <li><a href="/AdamAmsterdamFiles/${formBean.aaLink}">File:${formBean.feature.name}</a> <a
                        href="ZDB-PUB-050913-8">(1)</a></li>
            </c:if>
            <c:if test="${formBean.singleAffectedGeneFeature}">
                <li>
                    <a href="https://www.alliancegenome.org/allele/ZFIN:${formBean.feature.zdbID}">Alliance</a>
                </li>
            </c:if>
        </ul>
    </z:attributeListItem>


    <zfin2:entityNotesAttributeListItems entity="${formBean.feature}"/>

</z:attributeList>