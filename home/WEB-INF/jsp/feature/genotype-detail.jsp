<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<script type="text/javascript">

    function start_note(ref_page) {
        top.zfinhelp = open("/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-" + ref_page + ".apg", "notewindow", "scrollbars=no,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=325");
    }

    function popup_url(url) {
        open(url, "Description", "toolbar=yes,scrollbars=yes,resizable=yes");
    }

</script>

<zfin2:dataManager zdbID="${formBean.genotype.zdbID}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="genotype"/>


<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.genotype.handle}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.genotype.zdbID}"/>
    </tiles:insertTemplate>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th class="genotype-name-label">
            <c:if test="${!formBean.genotype.wildtype}">
                <span class="name-label">Genotype:</span>
            </c:if>
            <c:if test="${formBean.genotype.wildtype}">
                <span class="name-value">Wild-Type Line:</span>
            </c:if>
        </th>
        <td class="genotype-name-value">
            <span class="name-value"><zfin:name entity="${formBean.genotype}"/></span>
        </td>
    </tr>

    <c:if test="${formBean.genotype.wildtype}">
        <tr>
            <th>
                <span class="name-label">Abbreviation:</span>
            </th>
            <td>
                <span class="name-value">${formBean.genotype.handle}</span>
            </td>
        </tr>
    </c:if>

    <c:if test="${fn:length(formBean.genotype.aliases) ne null && fn:length(formBean.genotype.aliases) > 0}">
        <tr>
            <th>
                <c:choose>
                    <c:when test="${fn:length(formBean.genotype.aliases) > 1}">
                        Previous&nbsp;Names:
                    </c:when>
                    <c:otherwise>
                        Previous&nbsp;Name:
                    </c:otherwise>
                </c:choose>
            </th>
            <td>
                <c:forEach var="genoAlias" items="${formBean.genotype.aliases}" varStatus="loop">
                    ${genoAlias.alias}
                    <c:if test="${!loop.last}">,&nbsp;</c:if>
                    <c:if test="${genoAlias.publicationCount > 0}">
                        <c:choose>
                            <c:when test="${genoAlias.publicationCount == 1}">
                                (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${genoAlias.singlePublication.zdbID}">${genoAlias.publicationCount}</a>)
                            </c:when>
                            <c:otherwise>
                                (<a href="alias-publication-list?genoAlias.zdbID=${genoAlias.zdbID}&orderBy=author">${genoAlias.publicationCount}</a>)
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </c:forEach>
            </td>
        </tr>
    </c:if>

    <c:if test="${!formBean.genotype.wildtype}">
        <tr>
            <th>
                Background:
            </th>
            <td>
                <c:choose>
                    <c:when test="${fn:length(formBean.genotype.associatedGenotypes) ne null && fn:length(formBean.genotype.associatedGenotypes) > 0}">
                        <c:forEach var="background" items="${formBean.genotype.associatedGenotypes}" varStatus="loop">
                            <zfin:link entity="${background}"/>
                            <c:if test="${background.handle != background.name}">(${background.handle})</c:if>
                            <c:if test="${!loop.last}">,&nbsp;</c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        Unspecified
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <th>
                <c:choose>
                    <c:when test="${fn:length(formBean.genotypeStatistics.affectedMarkers) ne null && fn:length(formBean.genotypeStatistics.affectedMarkers) > 1}">
                        Affected&nbsp;Genes:
                    </c:when>
                    <c:otherwise>
                        Affected&nbsp;Gene:
                    </c:otherwise>
                </c:choose>
            </th>
            <td>
                <c:forEach var="affectedGene" items="${formBean.genotypeStatistics.affectedMarkers}" varStatus="loop">
                    <zfin:link entity="${affectedGene}"/>
                    <c:if test="${!loop.last}">,&nbsp;</c:if>
                </c:forEach>
            </td>
        </tr>
    </c:if>

    <tr>
        <th>
            <c:choose>
                <c:when test="${fn:length(formBean.genotype.suppliers) ne null && fn:length(formBean.genotype.suppliers) > 1}">
                    Current&nbsp;Sources:
                </c:when>
                <c:otherwise>
                    Current&nbsp;Source:
                </c:otherwise>
            </c:choose>
        </th>
        <td>
            <c:choose>
                <c:when test="${formBean.genotype.suppliers ne null && fn:length(formBean.genotype.suppliers) > 0}">
                    <c:forEach var="supplier" items="${formBean.genotype.suppliers}" varStatus="status">
                        <c:choose>
                            <c:when test="${formBean.genotype.extinct}">
                                ${supplier.organization.name}&nbsp;&nbsp;<font size="3" color="red">Extinct</font><img src="/images/warning-noborder.gif" border="0" alt="extinct" width="20" align="top" height="20">&nbsp;
                            </c:when>
                            <c:otherwise>
                                <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${supplier.organization.zdbID}"
                                   id="${supplier.organization.zdbID}">
                                        ${supplier.organization.name}</a>
                                <c:if test="${supplier.availState ne null}">(${supplier.availState})</c:if>&nbsp;
                                <c:choose>
                                    <c:when test="${supplier.moensLab}">&nbsp;
                                    <c:forEach var="affectedGene" items="${formBean.genotypeStatistics.affectedMarkers}"
                                               varStatus="loop">
                                        (<a href="http://labs.fhcrc.org/moens/Tilling_Mutants/${affectedGene.abbreviation}"><font size="-1">request this mutant</font></a>)
                                        <c:if test="${!loop.last}">,&nbsp;</c:if>

                                    </c:forEach>
                                    </c:when>
                                    <c:when test="${supplier.solnicaLab}">&nbsp;
                                        <c:forEach var="affectedGene"
                                                   items="${formBean.genotypeStatistics.affectedMarkers}"
                                                   varStatus="loop">
                                            (<a href="http://devbio.wustl.edu/solnicakrezellab/${affectedGene.abbreviation}.htm"><font size="-1">request this mutant</font></a>)
                                            <c:if test="${!loop.last}">,&nbsp;</c:if>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <zfin2:orderThis accessionNumber="${formBean.genotype.zdbID}"
                                                         organization="${supplier.organization}"/>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>

                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${formBean.genotype.extinct}">
                            <font size="3" color="red">extinct</font> <img src="/images/warning-noborder.gif"
                                                                           alt="extinct" width="20" align="top"
                                                                           height="20">
                        </c:when>
                        <c:otherwise>
                            No data available
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>


<authz:authorize ifAnyGranted="root">
    <div class="summary">
        <table class="summary">
            <tr>
                <th><b>Curator Note:</b></th>
            </tr>
            <c:forEach var="dataNote" items="${formBean.genotype.sortedDataNotes}" varStatus="loopCurNote">
                <tr>
                    <td>${dataNote.curator.name}&nbsp;&nbsp;${dataNote.date}<br/>${dataNote.note}
                        <c:if test="${!loopCurNote.last}"><br/>&nbsp;<br/></c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
</authz:authorize>

<c:if test="${formBean.genotype.externalNotes ne null && fn:length(formBean.genotype.externalNotes) > 0 }">
    <div class="summary">
        <b>Note:</b>
        <c:forEach var="extNote" items="${formBean.genotype.externalNotes}">
            <div>
                    ${extNote.note}
                <c:if test="${extNote.singlePubAttribution ne null}">
                    &nbsp;(<a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${extNote.singlePubAttribution.publication.zdbID}'>1</a>)
                </c:if>
            </div>
        </c:forEach>
    </div>
</c:if>

<c:if test="${!formBean.genotype.wildtype}">
<div class="summary">
    <b>GENOTYPE COMPOSITION</b>
    <c:choose>
        <c:when test="${formBean.genotypeFeatures ne null && fn:length(formBean.genotypeFeatures) > 0}">
            <table class="summary rowstripes">
                <tbody>
                <tr>
                    <th width="20%">
                        Feature
                    </th>
                    <th width="20%">
                        Zygosity
                    </th>
                    <th width="20%">
                        Parental Genotype
                    </th>
                    <th width="20%">
                        Lab of Origin
                    </th>
                    <th width="20%">
                        Construct
                    </th>

                </tr>
                <c:forEach var="genoFeat" items="${formBean.genotypeFeatures}" varStatus="loop">
                    <zfin:alternating-tr loopName="loop">
                        <td>
                            <c:choose>
                                <c:when test="${genoFeat.feature.numberOfRelatedGenotypes > 1}">
                                    <zfin:link entity="${genoFeat.feature}"/> &nbsp; <i><a
                                        href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fishselect.apg&query_results=exists&compareAllele=starts&fsel_allele_id=${genoFeat.feature.zdbID}">(in ${genoFeat.feature.numberOfRelatedGenotypes}
                                    genotypes)</a></i>
                                </c:when>
                                <c:otherwise>
                                    <zfin:link entity="${genoFeat.feature}"/> &nbsp; <i>(in 1 genotype)</i>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                                ${genoFeat.zygosity.name}
                        </td>
                        <td>
                                ${genoFeat.parentalZygosityDisplay}
                        </td>
                        <td>
                            <c:forEach var="source" items="${genoFeat.feature.sources}" varStatus="status">
                                <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${source.organization.zdbID}">
                                        ${source.organization.name}
                                </a>
                                <c:if test="${!status.last}">,&nbsp;</c:if>
                            </c:forEach>
                        </td>
                        <td>
                            <c:forEach var="construct" items="${genoFeat.feature.constructs}"
                                       varStatus="constructsloop">
                                <a href="/action/marker/view/${construct.marker.zdbID}">${construct.marker.name}</a>
                                <c:if test="${!constructsloop.last}">
                                    ,&nbsp;
                                </c:if>
                            </c:forEach>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>

                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <br>No data available</br>
        </c:otherwise>
    </c:choose>
</div>
<div class="summary">
    <b>GENE EXPRESSION</b>&nbsp;
    <small><a class="popup-link info-popup-link" href="/action/marker/note/expression"></a></small>
    <br/>
    <b>Gene expression in <zfin:name entity="${formBean.genotype}"/><c:if
            test="${fn:length(formBean.genotype.associatedGenotypes) ne null && fn:length(formBean.genotype.associatedGenotypes) > 0}">
        <c:forEach var="background" items="${formBean.genotype.associatedGenotypes}" varStatus="loop">
            (${background.handle})
            <c:if test="${!loop.last}">,&nbsp;</c:if>
        </c:forEach>
    </c:if> </b>
    <c:choose>
        <c:when test="${formBean.numberOfExpressionDisplays > 0 }">
            <table width="100%" class="summary rowstripes">
                <tbody>
                <tr>
                    <th width="16%">
                        Expressed Gene
                    </th>
                    <th width="32%">
                        Structure
                    </th>
                    <th width="17%">
                        Conditions
                    </th>
                    <th width="35%">
                        Figures
                    </th>
                </tr>
                <c:forEach var="xp" items="${formBean.expressionDisplays}" varStatus="loop" end="4">
                    <zfin:alternating-tr loopName="loop"
                                         groupBeanCollection="${formBean.expressionDisplays}"
                                         groupByBean="expressedGene">
                        <td valign="top">
                            <zfin:groupByDisplay loopName="loop"
                                                 groupBeanCollection="${formBean.expressionDisplays}"
                                                 groupByBean="expressedGene">
                                <zfin:link entity="${xp.expressedGene}"/>
                            </zfin:groupByDisplay>
                        </td>
                        <td valign="top">
                            <zfin2:toggledPostcomposedList expressionResults="${xp.expressionResults}" maxNumber="3"
                                                           id="${xp.expressedGene.zdbID}"/>
                        </td>
                        <td valign="top">
                            <zfin:link entity="${xp.experiment}"/>
                        </td>
                        <td valign="top">
                            <c:choose>
                                <c:when test="${(xp.numberOfFigures >1) && !xp.experiment.standard && !xp.experiment.chemical}">
                                    <a href='/action/expression/genotype-figure-summary?genoZdbID=${formBean.genotype.zdbID}&expZdbID=${xp.experiment.zdbID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                            ${xp.numberOfFigures} figures</a>
                                </c:when>
                                <c:when test="${(xp.numberOfFigures >1) && xp.experiment.standard && !xp.experiment.chemical}">
                                    <a href='/action/expression/genotype-figure-summary-standard?genoZdbID=${formBean.genotype.zdbID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                            ${xp.numberOfFigures} figures</a>
                                </c:when>
                                <c:when test="${(xp.numberOfFigures >1) && !xp.experiment.standard && xp.experiment.chemical}">
                                    <a href='/action/expression/genotype-figure-summary-chemical?genoZdbID=${formBean.genotype.zdbID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                            ${xp.numberOfFigures} figures</a>
                                </c:when>
                                <c:otherwise>
                                    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${xp.singleFigure.zdbID}'>
                                        <zfin2:figureOrTextOnlyLink figure="${xp.singleFigure}"
                                                                    integerEntity="${xp.numberOfFigures}"/></a>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${xp.imgInFigure}"><img src="/images/camera_icon.gif" border="0"
                                                                alt="with image">&nbsp;</c:if>from
                            <c:choose>
                                <c:when test="${xp.numberOfPublications > 1 }">
                                    ${xp.numberOfPublications} publications
                                </c:when>
                                <c:otherwise>
                                    <zfin:link entity="${xp.singlePublication}"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>

                </tbody>
            </table>

            <c:if test="${formBean.numberOfExpressionDisplays > 5}">
                <table width="100%">
                    <tbody>
                    <tr align="left">
                        <td>
                            Show all <a
                                href="show_all_expression?genoID=${formBean.genotype.zdbID}">${formBean.totalNumberOfExpressedGenes}&nbsp;expressed
                            genes</a>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </c:if>
        </c:when>

        <c:otherwise>
            <br>No data available</br>
        </c:otherwise>
    </c:choose>
</div>

<div class="summary">
    <b>PHENOTYPE</b>&nbsp;
    <small><a class='popup-link info-popup-link' href='/action/marker/note/phenotype'></a></small>
    <br/>
    <b>Phenotype in <zfin:name entity="${formBean.genotype}"/><c:if
            test="${fn:length(formBean.genotype.associatedGenotypes) ne null && fn:length(formBean.genotype.associatedGenotypes) > 0}">
        <c:forEach var="background" items="${formBean.genotype.associatedGenotypes}" varStatus="loop">
            (${background.handle})
            <c:if test="${!loop.last}">,&nbsp;</c:if>
        </c:forEach>
    </c:if></b>
    <c:choose>
        <c:when test="${formBean.numberOfPhenoDisplays > 0 }">
            <table class="summary rowstripes">
                <tbody>
                <tr>
                <th width="48%">
                    Phenotype
                </th>
                <th width="17%">
                    Conditions
                </th>
                <th width="35%">
                    Figures
                </th>
                </th>
                <c:forEach var="pheno" items="${formBean.phenoDisplays}" varStatus="loop" end="4">
                    <zfin:alternating-tr loopName="loop">
                        <td valign="top">
                            <zfin:link entity="${pheno.phenoStatement}"/>
                        </td>
                        <td valign="top">
                            <zfin:link entity="${pheno.experiment}"/>
                        </td>
                        <td valign="top">
                            <c:forEach var="figsPub" items="${pheno.figuresPerPub}">
                                <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                                    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${fig.zdbID}'><zfin2:figureOrTextOnlyLink
                                            figure="${fig}" integerEntity="1"/></a>
                                    <c:if test="${!fig.imgless}"><img src="/images/camera_icon.gif" border="0"
                                                                      alt="with image"></c:if>
                                    <c:if test="${!figloop.last}">,&nbsp;</c:if>
                                </c:forEach>
                                from <zfin:link entity="${figsPub.key}"/><br/>
                            </c:forEach>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>

                </tbody>
            </table>

            <c:if test="${formBean.numberOfPhenoDisplays > 5}">
                <table width="100%">
                    <tbody>
                    <tr align="left">
                        <td>
                            Show all <a
                                href="show_all_phenotype?zdbID=${formBean.genotype.zdbID}">${formBean.numberOfPhenoDisplays}&nbsp;experiments</a>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </c:if>
        </c:when>

        <c:otherwise>
            <br>No data available</br>
        </c:otherwise>
    </c:choose>
</div>
<p/>
<a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${formBean.genotype.zdbID}'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.totalNumberOfPublications})

</c:if>


