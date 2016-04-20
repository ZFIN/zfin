<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<script src="/javascript/table-collapse.js"></script>
<script type="text/javascript">

    function start_note(ref_page) {
        top.zfinhelp = open("/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-" + ref_page + ".apg", "notewindow", "scrollbars=no,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=325");
    }

    function popup_url(url) {
        open(url, "Description", "toolbar=yes,scrollbars=yes,resizable=yes");
    }

    jQuery(function () {
        jQuery("#gene-expression").tableCollapse({label: "expressed genes"});
        jQuery("#phenotype").tableCollapse({label: "phenotypes"});
    });
</script>

<zfin2:dataManager zdbID="${formBean.genotype.zdbID}" rtype="genotype"/>


<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.genotype.name}"/>
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

    <c:if test="${fn:length(formBean.previousNames) ne null && fn:length(formBean.previousNames) > 0}">
        <zfin2:previousNamesFast label="Previous Name" previousNames="${formBean.previousNames}"/>
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
                <zfin2:pluralization list="${affectedMarkerList}" singular="Affected Gene:"
                                     nonSingular="Affected Genes:"/>
            </th>
            <td>
                <c:forEach var="affectedGene" items="${affectedMarkerList}" varStatus="loop">
                    <zfin:link entity="${affectedGene}"/><c:if test="${!loop.last}">,&nbsp;</c:if>
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
                                <a href="/action/profile/view/${supplier.organization.zdbID}"
                                   id="${supplier.organization.zdbID}">
                                        ${supplier.organization.name}</a>
                                <c:if test="${supplier.availState ne null}">(${supplier.availState})</c:if>
                                <c:choose>
                                    <c:when test="${supplier.moensLab}">&nbsp;
                                        <c:forEach var="affectedGene"
                                                   items="${formBean.fishStatistics.affectedMarkers}"
                                                   varStatus="loop">
                                            (<a href="http://labs.fhcrc.org/moens/Tilling_Mutants/${affectedGene.abbreviation}"><font size="-1">request this mutant</font></a>)
                                            <c:if test="${!loop.last}">,&nbsp;
                                            </c:if>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${supplier.solnicaLab}">&nbsp;
                                            <c:forEach var="affectedGene"
                                                       items="${formBean.fishStatistics.affectedMarkers}"
                                                       varStatus="loop">
                                                (<a href="http://devbio.wustl.edu/solnicakrezellab/${affectedGene.abbreviation}.htm"><font size="-1">request this mutant</font></a>)
                                                <c:if test="${!loop.last}">,&nbsp;</c:if>
                                            </c:forEach>
                                        </c:if>
                                        <zfin2:orderThis accessionNumber="${formBean.genotype.zdbID}"
                                                         organization="${supplier.organization}"/>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${!status.last}"><br/></c:if>
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


<authz:authorize access="hasRole('root')">
    <div class="summary">
        <table class="summary">
            <tr>
                <th><b>Curator Note:</b></th>
            </tr>
            <c:forEach var="dataNote" items="${formBean.genotype.sortedDataNotes}" varStatus="loopCurNote">
                <tr>
                    <td>${dataNote.curator.fullName}&nbsp;
                        <fmt:formatDate value="${dataNote.date}" pattern="yyyy/MM/dd hh:mm"/>:
                            ${dataNote.note}
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
                    &nbsp;(<a href='/${extNote.publication.zdbID}'>1</a>)
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
                            Genomic Feature
                        </th>
                        <th width="20%">
                            Construct
                        </th>
                        <th width="20%">
                            Lab of Origin
                        </th>
                        <th width="20%">
                            Zygosity
                        </th>
                        <th width="20%">
                            Parental Zygosity
                        </th>
                    </tr>
                    <c:forEach var="genoFeat" items="${formBean.genotypeFeatures}" varStatus="loop">
                        <zfin:alternating-tr loopName="loop">
                            <td>
                                <zfin:link entity="${genoFeat.feature}"/>
                            </td>
                            <td>
                                <c:forEach var="construct" items="${genoFeat.feature.constructs}"
                                           varStatus="constructsloop">

                                    <a href="/action/marker/view/${construct.marker.zdbID}"><i>${construct.marker.name}</i></a><c:if
                                        test="${!constructsloop.last}">,&nbsp;</c:if>
                                </c:forEach>
                            </td>
                            <td>
                                <c:forEach var="source" items="${genoFeat.feature.sources}" varStatus="status">
                                    <c:if test="${source.organization.zdbID != 'ZDB-LAB-000914-1'}">
                                        <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${source.organization.zdbID}">
                                                ${source.organization.name}
                                        </a>
                                    </c:if>
                                    <c:if test="${!status.last}">,&nbsp;</c:if>
                                </c:forEach>
                            </td>
                            <td>
                                    ${genoFeat.zygosity.name}
                            </td>
                            <td>
                                    ${genoFeat.parentalZygosityDisplay}
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
    <div id="fish" class="summary">
        <zfin2:subsection title="FISH UTILIZING" showNoData="true"
                          test="${fn:length(fishList) ne null && fn:length(fishList) > 0}"
                          titleEntityAppended="${formBean.genotype}">
            <table class="summary rowstripes">
                <tr>
                    <th>Fish</th>
                    <th>Affected Genes</th>
                    <th>Phenotype</th>
                    <th>Gene Expression</th>
                </tr>
                <c:forEach var="fishGenotypeStatistics" items="${fishList}" varStatus="index">
                    <zfin:alternating-tr loopName="index">
                        <td><zfin:link entity="${fishGenotypeStatistics.fish}"/></td>
                        <td>
                            <c:forEach var="marker" items="${fishGenotypeStatistics.getAffectedMarker()}"
                                       varStatus="loop">
                                <zfin:link entity="${marker}"/><c:if test="${!loop.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td>
                            <zfin2:showFigureData entity="${genotype}"
                                                  fishGenotypeStatistics="${fishGenotypeStatistics.fishGenotypePhenotypeStatistics}"
                                                  link="/action/fish/phenotype-summary?fishID=${fishGenotypeStatistics.fish.zdbID}&imagesOnly=false"/>
                        </td>
                        <td>
                            <zfin2:showFigureData entity="${genotype}"
                                                  fishGenotypeStatistics="${fishGenotypeStatistics.fishGenotypeExpressionStatistics}"
                                                  link="/action/expression/fish-expression-figure-summary?fishID=${fishGenotypeStatistics.fish.zdbID}&imagesOnly=false"/>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>
            </table>
        </zfin2:subsection>
    </div>

    <p/>
    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${formBean.genotype.zdbID}&rtype=genotype'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.totalNumberOfPublications})

</c:if>


