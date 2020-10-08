<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<meta name="feature-detail-page"/>

<zfin2:dataManager zdbID="${formBean.feature.zdbID}"
                   prototypeURL="/action/feature/view/prototype/${formBean.feature.zdbID}"/>

<table class="two-column-info">
    <tr>
        <td>
            <table class="primary-entity-attributes">
                <tr>
                    <th class="genotype-name-label">
                        <span class="name-label">Genomic Feature:</span>
                    </th>
                    <td>
                        <span class="name-value">${formBean.feature.name}</span>
                    </td>
                </tr>

                <c:if test="${fn:contains(formBean.feature.abbreviation, 'unrecovered')==true}">
                    <br style="font-size:small;"> Note: This record has been created to support data for unrecovered alleles reported by a TILLING project. </br>
                </c:if>

                <c:if test="${formBean.feature.type.unspecified}">
                    <br style="font-size:small;"> Note: Unspecified genomic feature records have been created in support of data for which a publication has not specified a genomic feature. </br>
                </c:if>

                <c:if test="${formBean.feature.aliases != null}">
                    <tr>
                        <th>
                            Synonyms:
                        </th>
                        <td>
                            <c:forEach var="featureAlias" items="${formBean.feature.aliases}" varStatus="loop">
                                ${featureAlias.alias}
                                <c:choose>
                                    <c:when test="${featureAlias.publicationCount > 0}">
                                        <c:choose>
                                            <c:when test="${featureAlias.publicationCount == 1}">
                                                (<a href="/${featureAlias.singlePublication.zdbID}">${featureAlias.publicationCount}</a>)<c:if
                                                    test="${!loop.last}">, </c:if>
                                            </c:when>
                                            <c:otherwise>
                                                (<a href="/action/publication/list/${featureAlias.zdbID}">${featureAlias.publicationCount}</a>)<c:if
                                                    test="${!loop.last}">, </c:if>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise><c:if test="${!loop.last}">, </c:if>
                                    </c:otherwise>
                                </c:choose>

                            </c:forEach>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <th>
                        Affected Genomic Regions:
                    </th>
                    <c:choose>
                        <c:when test="${fn:length(formBean.feature.affectedGenes) > 0 }">
                            <td>
                                    <%--<c:forEach var="affectedGene" items="${formBean.feature.affectedGenes}"
                                               varStatus="loop">
                                        <zfin:link entity="${affectedGene}"/>--%>
                                <c:forEach var="mRel" items="${formBean.feature.affectedGenesReln}" varStatus="loop">
                                    <a href="/${mRel.marker.zdbID}"><i>${mRel.marker.abbreviation}</i></a>


                                    <c:if test="${mRel.publicationCount > 0}">
                                        <c:choose>
                                            <c:when test="${mRel.publicationCount == 1}">
                                                (<a href="/${mRel.singlePublication.zdbID}">${mRel.publicationCount}</a>)<c:if
                                                    test="${!loop.last}">, </c:if>
                                            </c:when>
                                            <c:otherwise>
                                                (<a href="/action/infrastructure/data-citation-list/${mRel.zdbID}">${mRel.publicationCount}</a>)<c:if
                                                    test="${!loop.last}">, </c:if>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
                                </c:forEach>

                                    <%--</c:forEach>--%>
                            </td>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${formBean.feature.type.transgenic}">
                                <c:if test="${!(formBean.feature.knownInsertionSite)}">
                                    <td>
                                        This feature is representative of one or more unknown insertion sites.
                                    </td>
                                </c:if>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </tr>

                <c:if test="${formBean.feature.type.transgenic}">
                    <tr>
                        <th>
                            Construct:
                        </th>
                        <td>
                            <c:forEach var="mRel" items="${formBean.sortedConstructRelationships}" varStatus="loop">
                                <a href="/${mRel.marker.zdbID}"><i>${mRel.marker.name}</i></a>
                                <%--//<zfin:name entity="${mRel.marker}"/>--%>

                                <c:if test="${mRel.publicationCount > 0}">
                                    <c:choose>
                                        <c:when test="${mRel.publicationCount == 1}">
                                            (<a href="/${mRel.singlePublication.zdbID}">${mRel.publicationCount}</a>)<c:if
                                                test="${!loop.last}">, </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            (<a href="/action/infrastructure/data-citation-list/${mRel.zdbID}">${mRel.publicationCount}</a>)<c:if test="${!loop.last}">, </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                        </td>
                    </tr>
                </c:if>

                <tr>
                    <th>
                        Type:
                    </th>
                    <td>
                        ${formBean.feature.type.display}
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
                    </td>
                </tr>

                <tr>
                    <th>
                        Protocol:
                    </th>
                    <td>
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
                                    <c:forEach var="createdBy" items="${formBean.createdByRelationship}"
                                               varStatus="loop">
                                        <zfin:link entity="${createdBy.marker}"/>

                                        <c:if test="${!loop.last}">,&nbsp;</c:if>

                                    </c:forEach>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${formBean.createdByRelationship ne null && fn:length(formBean.createdByRelationship) > 0}">
                                        ${mutagee.toString()} treated with
                                        <c:forEach var="createdBy" items="${formBean.createdByRelationship}"
                                                   varStatus="loop">
                                            <zfin:link entity="${createdBy.marker}"/>

                                            <c:if test="${!loop.last}">,&nbsp;</c:if>

                                        </c:forEach>

                                    </c:when>
                                    <c:otherwise>
                                        ${mutagee.toString()} treated with ${mutagen.toString()}
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>

                <tr>
                    <th>
                        Lab Of Origin:
                    </th>
                    <td>
                        <c:choose>
                            <c:when test="${formBean.feature.sources ne null && fn:length(formBean.feature.sources) > 0}">
                                <c:forEach var="source" items="${formBean.feature.sources}" varStatus="status">
                                    <c:if test="${source.organization.zdbID != 'ZDB-LAB-000914-1'}">
                                        <zfin:link entity="${source.organization}"/>
                                    </c:if>
                                </c:forEach>
                            </c:when>
                        </c:choose>
                    </td>
                </tr>

                <tr>
                    <th>
                        Location:
                    </th>
                    <td>
                        <c:choose>
                            <c:when test="${fn:length(formBean.featureLocations)>0}">

                                <zfin2:displayFullLocation location="${formBean.featureLocations[0]}"
                                                           hideLink="${empty formBean.feature.affectedGenes}"/>

                            </c:when>
                            <c:otherwise>
                                <zfin2:displayLocation entity="${formBean.feature}"
                                                       hideLink="${empty formBean.feature.affectedGenes}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>

                </tr>

                <tr>
                    <th>
                        Sequence:
                    </th>
                    <td>
                        <c:forEach var="featureGenbankLink" items="${formBean.genbankDbLinks}" varStatus="loop">
                            <zfin:link entity="${featureGenbankLink}"/>
                            <c:if test="${featureGenbankLink.publicationCount > 0}">
                                <c:choose>
                                    <c:when test="${featureGenbankLink.publicationCount == 1}">
                                        (<a href="/${featureGenbankLink.singlePublication.zdbID}">${featureGenbankLink.publicationCount}</a>)
                                    </c:when>
                                    <c:otherwise>
                                        (<a href="/action/infrastructure/data-citation-list/${featureGenbankLink.zdbID}">${featureGenbankLink.publicationCount}</a>)
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <c:if test="${!loop.last}">,&nbsp;</c:if>
                        </c:forEach>
                    </td>
                </tr>

                <tr>
                    <th>
                        Current Sources:
                    </th>
                    <td>
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
                                    <c:if test="${supplier.moensLab}">&nbsp;(<a href="http://labs.fhcrc.org/moens/Tilling_Mutants/${formBean.feature.singleRelatedMarker.abbreviation}"><font size="-1">request this mutant</font></a>)
                                    </c:if>
                                    <c:if test="${supplier.solnicaLab}">&nbsp;(<a href="http://devbio.wustl.edu/solnicakrezellab/${formBean.feature.singleRelatedMarker.abbreviation}.htm"><font size="-1">request this mutant</font></a>)
                                    </c:if>
                                    <c:if test="${supplier.riken}">&nbsp;(<a href="http://www.shigen.nig.ac.jp/zebrafish/strainDetailAction.do?zfinId=${formBean.feature.singleRelatedGenotype.zdbID}"><font size="-1">order this</font></a>)
                                    </c:if>
                                    <c:if test="${!status.last}"><br/></c:if>
                                </c:forEach>
                            </c:when>
                            <c:when test="${!empty formBean.genotypeDisplays}">

                                <c:forEach var="genotypeDisplay" items="${formBean.genotypeDisplays}" varStatus="loop">
                                    <c:if test="${genotypeDisplay.genotype.extinct}">
                                        <font size="3" color="red">extinct</font> <i class="warning-icon"
                                                                                     title="extinct"></i>
                                    </c:if>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <span class="no-data-tag"></span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>

                <zfin2:entityNotes entity="${formBean.feature}"/>
            </table>
        </td>
        <td>
            <div class="gbrowse-image"></div>
        </td>
    </tr>
</table>

<zfin2:subsection title="MUTATION DETAILS"
                  showNoData="true"
                  test="${!empty formBean.mutationDetails.dnaChangeStatement ||
                          !empty formBean.mutationDetails.transcriptChangeStatement ||
                          !empty formBean.mutationDetails.proteinChangeStatement}">
    <table class="summary horizontal-solidblock">
        <c:if test="${!empty formBean.mutationDetails.dnaChangeStatement}">
            <tr>
                <th>DNA/cDNA Change:</th>
                <td>
                        ${formBean.mutationDetails.dnaChangeStatement}
                    <c:choose>
                        <c:when test="${fn:length(formBean.dnaChangeAttributions) == 1}">
                            (<a href="/${formBean.dnaChangeAttributions[0].sourceZdbID}">1</a>)
                        </c:when>
                        <c:when test="${fn:length(formBean.dnaChangeAttributions) > 1}">
                            (<a href="/action/feature/${formBean.feature.zdbID}/mutation-detail-citations?type=dna">${fn:length(formBean.dnaChangeAttributions)}</a>)
                        </c:when>
                    </c:choose>
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty formBean.mutationDetails.transcriptChangeStatement}">
            <tr>
                <th>Transcript Consequence:</th>
                <td>
                        ${formBean.mutationDetails.transcriptChangeStatement}
                    <c:choose>
                        <c:when test="${fn:length(formBean.transcriptConsequenceAttributions) == 1}">
                            (<a href="/${formBean.transcriptConsequenceAttributions[0].sourceZdbID}">1</a>)
                        </c:when>
                        <c:when test="${fn:length(formBean.transcriptConsequenceAttributions) > 1}">
                            (<a href="/action/feature/${formBean.feature.zdbID}/mutation-detail-citations?type=transcript">${fn:length(formBean.transcriptConsequenceAttributions)}</a>)
                        </c:when>
                    </c:choose>
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty formBean.mutationDetails.proteinChangeStatement}">
            <tr>
                <th>Protein Consequence:</th>
                <td>
                        ${formBean.mutationDetails.proteinChangeStatement}
                    <c:choose>
                        <c:when test="${fn:length(formBean.proteinConsequenceAttributions) == 1}">
                            (<a href="/${formBean.proteinConsequenceAttributions[0].sourceZdbID}">1</a>)
                        </c:when>
                        <c:when test="${fn:length(formBean.proteinConsequenceAttributions) > 1}">
                            (<a href="/action/feature/${formBean.feature.zdbID}/mutation-detail-citations?type=protein">${fn:length(formBean.proteinConsequenceAttributions)}</a>)
                        </c:when>
                    </c:choose>
                </td>
            </tr>
        </c:if>
    </table>
</zfin2:subsection>

<c:set var="title">
    FLANKING SEQUENCE (GENOMIC) <a class='popup-link info-popup-link' href='/action/feature/flank-seq'></a>
</c:set>

<zfin2:subsection title="${title}"
                  showNoData="true"
                  test="${!empty formBean.feature.featureGenomicMutationDetailSet && !(formBean.varSequence.vfsVariation.contains('null'))}">
    <table style="border: 1px solid black; width:100%; word-wrap:break-word;table-layout:fixed;>
              table-layout: fixed;">
        <c:if test="${!empty formBean.varSequence}">


            <tr>
            <tr>
                <td>
                        ${formBean.varSequence.vfsLeftEnd}
                </td>
            </tr>
            <tr>
                <td>
                       <span style="color: red;">
                               ${formBean.varSequence.vfsVariation}
                       </span>
                </td>
            </tr>

            <tr>
                <td>
                    <c:if test="${formBean.varSequence.vfsVariation.length()>450}">
                        <zfin2:toggleTextLength text="${formBean.varSequence.vfsRightEnd}"
                                                idName="${zfn:generateRandomDomID()}"
                                                shortLength="80"/>
                    </c:if>
                    <c:if test="${formBean.varSequence.vfsVariation.length()<450}">

                        ${formBean.varSequence.vfsRightEnd}
                    </c:if>
                    <c:if test="${empty formBean.varSequence.vfsVariation}">

                        ${formBean.varSequence.vfsRightEnd}
                    </c:if>

                </td>
            </tr>

            <tr>
                <td>
                    <c:choose>
                        <c:when test="${fn:length(formBean.varSeqAttributions) == 1}">
                            (<a href="/${formBean.varSeqAttributions[0].sourceZdbID}">1</a>)
                        </c:when>
                        <c:when test="${fn:length(formBean.varSeqAttributions) > 1}">
                            (<a href="/action/publication/list/${formBean.varSequence.zdbID}">${fn:length(formBean.varSeqAttributions)}</a>)
                        </c:when>
                    </c:choose>
                </td>
            </tr>

        </c:if>
    </table>
</zfin2:subsection>

<zfin2:externalNotes notes="${formBean.externalNotes}"/>

<c:set var="title">
    OTHER <zfin:abbrev entity="${formBean.feature}"/> PAGES
</c:set>
<div id="other-pages">
    <zfin2:subsection title="${title}" test="${!empty formBean.summaryPageDbLinks || !empty formBean.aaLink}"
                      showNoData="true" noDataText="No links to external sites">
        <table class="summary horizontal-solidblock">
            <c:forEach var="link" items="${formBean.summaryPageDbLinks}" varStatus="loop">
                <tr>
                    <td>
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
                    </td>
                </tr>
            </c:forEach>

            <c:if test="${!empty formBean.aaLink}">
                <tr>
                    <td>
                        <a href="/AdamAmsterdamFiles/${formBean.aaLink}">File:${formBean.feature.name}</a> <a
                            href="ZDB-PUB-050913-8">(1)</a></td>
                </tr>
            </c:if>
        </table>
    </zfin2:subsection>
</div>

<div id="genotype">
    <zfin2:subsection title="GENOTYPES" test="${!empty formBean.genotypeDisplays}" showNoData="true">
        <table id="genotypes-table" class="summary rowstripes">
            <tr>
                <th width="25%">
                    Zygosity
                </th>
                <th width="25%">
                    Genotype (Background)
                </th>
                <th width="25%">
                    Affected Genomic Regions
                </th>
                <th width="25%">
                    Parental Zygosity
                </th>
            </tr>

            <c:forEach var="genotypeDisplay" items="${formBean.genotypeDisplays}" varStatus="loop">
                <zfin:alternating-tr loopName="loop" groupBeanCollection="${formBean.genotypeDisplays}"
                                     groupByBean="zygosity" newGroup="true">
                    <td>
                        <zfin:groupByDisplay loopName="loop" groupBeanCollection="${formBean.genotypeDisplays}"
                                             groupByBean="zygosity">
                            ${genotypeDisplay.zygosity}
                        </zfin:groupByDisplay>
                    </td>
                    <td>
                        <zfin:link entity="${genotypeDisplay.genotype}"/>
                    </td>
                    <td>
                        <zfin:link entity="${genotypeDisplay.affectedGenes}"/>
                    </td>
                    <td>
                        <c:if test="${genotypeDisplay.zygosity ne 'Complex'}">${genotypeDisplay.parentalZygosityDisplay}</c:if>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </zfin2:subsection>
</div>

<c:if test="${!empty formBean.ftrCommContr}">
    <div id="communitycontrib">
        <zfin2:subsection title="SUPPLEMENTAL INFORMATION <a href='/${formBean.ZShareOrigPub.zdbID}'>(1)</a>"
                          showNoData="false">
            <table class="summary horizontal-solidblock">
                <tr>
                    <th>Functional Consequence:</th>
                    <td>${formBean.ftrCommContr.functionalConsequence.toString()}</td>
                </tr>

                <tr>
                    <th>Adult Viable:</th>
                    <td><zfin2:nullableBoolean value="${formBean.ftrCommContr.adultViable}"/></td>
                </tr>
                <tr>
                    <th>Maternal Zygosity Examined:</th>
                    <td><zfin2:nullableBoolean value="${formBean.ftrCommContr.maternalZygosityExamined}"/></td>
                </tr>
                <tr>
                    <th>NMD Apparent:</th>
                    <td>${formBean.ftrCommContr.nmdApparent.toString()}</td>
                </tr>
                <tr>
                    <th>Available:</th>
                    <td><zfin2:nullableBoolean value="${formBean.ftrCommContr.currentlyAvailable}"/></td>
                </tr>
                <tr>
                    <th>Other Line Information:</th>
                    <td>${formBean.ftrCommContr.otherLineInformation}</td>
                </tr>
            </table>
        </zfin2:subsection>
    </div>
</c:if>

<hr width="80%">
<a href='/action/publication/list/${formBean.feature.zdbID}'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.numPubs})

<script type="text/javascript">
    $('#genotype').tableCollapse({label: 'rows'});
    $(".gbrowse-image").gbrowseImage({
        width: 400,
        imageUrl: "${formBean.gBrowseImage.imageUrl}",
        linkUrl: "${formBean.gBrowseImage.linkUrl}",
        build: "${formBean.gBrowseImage.build}"
    });
</script>
