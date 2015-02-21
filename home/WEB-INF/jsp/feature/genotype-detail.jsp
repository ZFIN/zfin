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

    function expandExpression() {
        document.getElementById('expression-short-version').style.display = 'none';
        document.getElementById('expression-long-version').style.display = 'block';
    }

    function collapseExpression() {
        document.getElementById('expression-short-version').style.display = 'block';
        document.getElementById('expression-long-version').style.display = 'none';
    }


    function expandPhenotype() {
        document.getElementById('phenotype-short-version').style.display = 'none';
        document.getElementById('phenotype-long-version').style.display = 'block';
    }

    function collapsePhenotype() {
        document.getElementById('phenotype-short-version').style.display = 'block';
        document.getElementById('phenotype-long-version').style.display = 'none';
    }
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
                                                   items="${formBean.genotypeStatistics.affectedMarkers}"
                                                   varStatus="loop">
                                            (<a href="http://labs.fhcrc.org/moens/Tilling_Mutants/${affectedGene.abbreviation}"><font size="-1">request this mutant</font></a>)
                                            <c:if test="${!loop.last}">,&nbsp;
                                            </c:if>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${supplier.solnicaLab}">&nbsp;
                                            <c:forEach var="affectedGene"
                                                       items="${formBean.genotypeStatistics.affectedMarkers}"
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


<authz:authorize ifAnyGranted="root">
    <div class="summary">
        <table class="summary">
            <tr>
                <th><b>Curator Note:</b></th>
            </tr>
            <c:forEach var="dataNote" items="${formBean.genotype.sortedDataNotes}" varStatus="loopCurNote">
                <tr>
                    <td>${dataNote.curator.fullName}&nbsp;&nbsp;${dataNote.date}<br/>${dataNote.note}
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
                            Parental Genotype
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
                                    <a href="/action/marker/view/${construct.marker.zdbID}">${construct.marker.name}</a>
                                    <c:if test="${!constructsloop.last}">,&nbsp;</c:if>
                                </c:forEach>
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
        <div id="expression-short-version" class="summary">
            <c:choose>
                <c:when test="${formBean.expressionDisplays != null && fn:length(formBean.expressionDisplays) > 0 }">
                    <zfin2:expressionData expressionDisplays="${formBean.expressionDisplays}" showNumberOfRecords="5"
                                          showCondition="true" />
                    <c:if test="${fn:length(formBean.expressionDisplays) > 5}">
                        <div>
                            <a href="javascript:expandExpression()">
                                <img src="/images/darrow.gif" alt="expand" border="0">
                                Show all</a>
                            ${formBean.totalNumberOfExpressedGenes} expressed genes
                        </div>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <span class="no-data-tag">No data available</span>
                </c:otherwise>
            </c:choose>
        </div>
        <div style="display:none" id="expression-long-version" class="summary">
            <c:if test="${formBean.expressionDisplays != null && fn:length(formBean.expressionDisplays) > 0 }">
                <zfin2:expressionData expressionDisplays="${formBean.expressionDisplays}" showNumberOfRecords="${fn:length(formBean.expressionDisplays)}"
                                      showCondition="true" />
            </c:if>
            <div>
                <a href="javascript:collapseExpression()">
                    <img src="/images/up.gif" alt="expand" title="Show first 5 expressed genes" border="0">
                    Show first</a> 5 expressed genes
            </div>
        </div>
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

        <div id="phenotype-short-version" class="summary">
            <c:choose>
                <c:when test="${formBean.phenoDisplays != null && fn:length(formBean.phenoDisplays) > 0 }">
                    <zfin2:all-phenotype phenotypeDisplays="${formBean.phenoDisplays}" showNumberOfRecords="5"
                                         secondColumn="condition"/>
                    <c:if test="${fn:length(formBean.phenoDisplays) > 5}">
                        <div>
                            <a href="javascript:expandPhenotype()">
                                <img src="/images/darrow.gif" alt="expand" border="0">
                                Show all</a>
                            ${fn:length(formBean.phenoDisplays)} phenotypes
                        </div>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <span class="no-data-tag">No data available</span>
                </c:otherwise>
            </c:choose>
        </div>
        <div style="display:none" id="phenotype-long-version" class="summary">
            <c:if test="${formBean.phenoDisplays != null && fn:length(formBean.phenoDisplays) > 0 }">
                <zfin2:all-phenotype phenotypeDisplays="${formBean.phenoDisplays}" showNumberOfRecords="${fn:length(formBean.phenoDisplays)}"
                                     secondColumn="condition"/>
            </c:if>
            <div>
                <a href="javascript:collapsePhenotype()">
                    <img src="/images/up.gif" alt="expand" title="Show first 5 phenotypes" border="0">
                    Show first</a> 5 phenotypes
            </div>
        </div>
    </div>
    <p/>
    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${formBean.genotype.zdbID}&rtype=genotype'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.totalNumberOfPublications})

</c:if>


