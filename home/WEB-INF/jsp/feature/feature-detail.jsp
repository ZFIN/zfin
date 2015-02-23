<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<meta name="feature-detail-page"/>

<zfin2:dataManager zdbID="${formBean.feature.zdbID}" rtype="feature"/>

<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.feature.name}"/>
    </tiles:insertTemplate>
</div>

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
                <c:if test="${featureAlias.publicationCount > 0}">
                    <c:choose>
                        <c:when test="${featureAlias.publicationCount == 1}">
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${featureAlias.singlePublication.zdbID}">${featureAlias.publicationCount}</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${featureAlias.zdbID}&rtype=genotype">${featureAlias.publicationCount}</a>)
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${!loop.last}">,&nbsp;</c:if>
            </c:forEach>
        </td>
    </tr>
</c:if>
<tr>
    <th>
        Affected Genes:
    </th>
    <c:choose>
        <c:when test="${fn:length(formBean.sortedMarkerRelationships) > 0 }">
            <td>
                <c:forEach var="fmRel" items="${formBean.sortedMarkerRelationships}" varStatus="loop">
                    <zfin:link entity="${fmRel.marker}"/>
                    <c:if test="${fmRel.publicationCount > 0}">
                        <c:choose>
                            <c:when test="${fmRel.publicationCount == 1}">
                                (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${fmRel.singlePublication.zdbID}">${fmRel.publicationCount}</a>)
                            </c:when>
                            <c:otherwise>
                                (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${fmRel.zdbID}&rtype=genotype">${fmRel.publicationCount}</a>)
                            </c:otherwise>
                        </c:choose>
                    </c:if><c:if test="${!loop.last}">,&nbsp;</c:if>
                </c:forEach>
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
                <a href="/action/marker/view/${mRel.marker.zdbID}">${mRel.marker.name}</a>
                <%--//<zfin:name entity="${mRel.marker}"/>--%>

                <c:if test="${mRel.publicationCount > 0}">
                    <c:choose>
                        <c:when test="${mRel.publicationCount == 1}">
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${mRel.singlePublication.zdbID}">${mRel.publicationCount}</a>)<c:if test="${!loop.last}">,&nbsp;</c:if>        
                        </c:when>
                        <c:otherwise>
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${mRel.zdbID}&rtype=genotype">${mRel.publicationCount}</a>)<c:if test="${!loop.last}">,&nbsp;</c:if>
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
    <td>${formBean.feature.type.display}
        <c:if test="${fn:length(formBean.featureTypeAttributions) > 0 }">
            <c:choose>
                <c:when test="${fn:length(formBean.featureTypeAttributions)== 1 }">
                    (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${formBean.singlePublication}">${1}</a>)
                </c:when>
                <c:otherwise>
                    (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&rtype=genotype&recattrsrctype=feature+type&OID=${formBean.feature.zdbID}">${fn:length(formBean.featureTypeAttributions)}</a>)
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
                ${mutagen.toString()}&nbsp; <c:if test="${formBean.createdByRelationship ne null}"><zfin:link
                    entity="${formBean.createdByRelationship.marker}"/></c:if>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${formBean.createdByRelationship ne null}">
                        ${mutagee.toString()} treated with <zfin:link
                            entity="${formBean.createdByRelationship.marker}"/>
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
        <zfin2:displayLocation entity="${formBean.feature}"/>
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
                        (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${featureGenbankLink.singlePublication.zdbID}">${featureGenbankLink.publicationCount}</a>)
                    </c:when>
                    <c:otherwise>
                        (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${featureGenbankLink.zdbID}&rtype=genotype">${featureGenbankLink.publicationCount}</a>)
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
                    <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${supplier.organization.zdbID}"
                       id="${supplier.organization.zdbID}">
                        ${supplier.organization.name}</a>
                    <c:if test="${supplier.zirc || supplier.ezrc}">&nbsp;
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
            <c:otherwise>
                <span class="no-data-tag"></span>
            </c:otherwise>
        </c:choose>
    </td>
</tr>

<zfin2:notesInDiv hasNotes="${formBean.feature}"/>

</table>
<c:choose>
    <c:when test="${not empty formBean.summaryPageDbLinks}">
        <c:forEach var="featureSummaryDblink" items="${formBean.summaryPageDbLinks}">
            <div class="summary">
                <table id="other-feature-pages" class="summary">
                    <caption>OTHER <em>${formBean.feature.name}</em> FEATURE PAGES</caption>
                    <tr>
                        <td>

                            <zfin:link entity="${featureSummaryDblink}"/>
                            <c:if test="${featureSummaryDblink.publicationCount > 0}">
                                <c:choose>
                                    <c:when test="${featureSummaryDblink.publicationCount == 1}">
                                        (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${featureSummaryDblink.singlePublication.zdbID}">${featureSummaryDblink.publicationCount}</a>)
                                    </c:when>
                                    <c:otherwise>
                                        (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${featureSummaryDblink.zdbID}&rtype=genotype">${featureSummaryDblink.publicationCount}</a>)
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </td>
                    </tr>
                </table>
            </div>
        </c:forEach>
    </c:when>
    <%-- <c:otherwise>
                <p><strong>OTHER <em>${formBean.feature.name}</em> FEATURE PAGES</strong> <span class="no-data-tag">No links to external sites available</span></p>
            </c:otherwise>  --%>
</c:choose>

<div id="short-version" class="summary">
    <c:choose>
        <c:when test="${formBean.featgenoStats != null && fn:length(formBean.featgenoStats) > 0 }">
            <zfin2:genotype-information genotypes="${formBean.featgenoStats}" showNumberOfRecords="5"/>
            <c:if test="${fn:length(formBean.featgenoStats) > 5}">
                <div>
                    <a href="javascript:expand()">
                        <img src="/images/darrow.gif" alt="expand" border="0">
                        Show all</a>
                    ${fn:length(formBean.featgenoStats)} genotypes
                </div>
            </c:if>
        </c:when>
        <c:otherwise>
            <span><strong>GENOTYPES</strong></span> <span class="no-data-tag">No data available</span>
        </c:otherwise>
    </c:choose>
</div>
<div style="display:none" id="long-version" class="summary">
    <c:if test="${formBean.featgenoStats != null && fn:length(formBean.featgenoStats) > 0 }">
        <zfin2:genotype-information genotypes="${formBean.featgenoStats}"
                                    showNumberOfRecords="${fn:length(formBean.featgenoStats)}"/>
    </c:if>
    <div>
        <a href="javascript:collapse()">
            <img src="/images/up.gif" alt="expand" title="Show first 5 genotypes" border="0">
            Show first</a> 5 genotypes
    </div>
</div>

<hr width="80%">
<a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${formBean.feature.zdbID}&total_count=${formBean.numPubs}&rtype=genotype'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.numPubs})

<script type="text/javascript">
    function expand() {
        document.getElementById('short-version').style.display = 'none';
        document.getElementById('long-version').style.display = 'block';
    }

    function collapse() {
        document.getElementById('short-version').style.display = 'block';
        document.getElementById('long-version').style.display = 'none';
    }
</script>
