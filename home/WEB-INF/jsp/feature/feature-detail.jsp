<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<zfin2:dataManager zdbID="${formBean.feature.zdbID}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="feature"/>

<table width="100%" border="0">
<tr>
    <td width="180">
        <FONT SIZE=+1><STRONG>Genomic Feature:</STRONG></FONT>
    </td>
    <td>
        <FONT SIZE=+1><STRONG>
            ${formBean.feature.name}
        </STRONG></FONT>
    </td>

    <td align="right">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="${formBean.feature.name}"/>
            <tiles:putAttribute name="subjectID" value="${formBean.feature.zdbID}"/>
        </tiles:insertTemplate>
    </td>
</tr>

<c:if test="${fn:contains(formBean.feature.abbreviation, 'unrecovered')==true}">

    <br style="font-size:small;">  Note: This record has been created to support data for unrecovered alleles reported by a TILLING project.   </br>

</c:if>

<c:if test="${formBean.feature.type.unspecified}">


    <br style="font-size:small;">  Note: Unspecified genomic feature records have been created in support of data for which a publication has not specified a genomic feature.   </br>

</c:if>
<hr>
<c:if test="${formBean.feature.aliases != null}">
    <tr>
        <td>
            <b> Previous Names: </b>
        </td>
        <td>
            <c:forEach var="featureAlias" items="${formBean.feature.aliases}" varStatus="loop">
                ${featureAlias.alias}
                <c:if test="${featureAlias.publicationCount > 0}">
                    <c:choose>
                        <c:when test="${featureAlias.publicationCount == 1}">
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${featureAlias.singlePublication.zdbID}">${featureAlias.publicationCount}</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${featureAlias.zdbID}">${featureAlias.publicationCount}</a>)
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${!loop.last}">,&nbsp;</c:if>
            </c:forEach>
        </td>
    </tr>
</c:if>
<tr>

    <td width="180">
        <b><b>Affected Genes:</b> </b>
    </td>
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
                                (<a href="relationship-publication-list?featuremarkerRelationship.zdbID=${fmRel.zdbID}&orderBy=author">${fmRel.publicationCount}</a>)
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${!loop.last}">
                        ,&nbsp;
                    </c:if>
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
    <td width="180">
        <b>Construct:</b>
    </td>
    <td>
        <c:forEach var="mRel" items="${formBean.sortedConstructRelationships}" varStatus="loop">
            <a href="/action/marker/view/${mRel.marker.zdbID}">${mRel.marker.name}</a>
            <%--//<zfin:name entity="${mRel.marker}"/>--%>

            <c:if test="${mRel.publicationCount > 0}">
                <c:choose>
                    <c:when test="${mRel.publicationCount == 1}">
                        (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${mRel.singlePublication.zdbID}">${mRel.publicationCount}</a>)
                    </c:when>
                    <c:otherwise>
                        (<a href="relationship-publication-list?featuremarkerRelationship.zdbID=${mRel.zdbID}&orderBy=author">${mRel.publicationCount}</a>)
                    </c:otherwise>
                </c:choose>
            </c:if>
            <c:if test="${!loop.last}">
                ,&nbsp;
            </c:if>
        </c:forEach>
    </td>


    </c:if>
<tr>
    <td width="180">
        <b>Type:</b>
    </td>
    <td>  ${formBean.feature.type.display}
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
    <td>
        <b>Protocol:</b>
    </td>
    <td>
        <c:choose>
            <c:when test="${formBean.feature.featureAssay.mutagen eq null}" >
                Not Specified
            </c:when>
            <c:when test="${formBean.feature.featureAssay.mutagen=='not specified'}" >
                Not Specified
            </c:when>
            <c:when test="${formBean.feature.featureAssay.mutagee=='not Specified'}" >

                <c:if test ="${formBean.feature.featureAssay.mutagen =='not Specified'}">
                    Not Specified
                </c:if>
            </c:when>
            <c:when test="${formBean.feature.featureAssay.mutagee=='Not Specified'}" >
                <c:if test ="${formBean.feature.featureAssay.mutagen ne 'spontaneous'}">
                    treated with     ${formBean.feature.featureAssay.mutagen}
                </c:if>
                <c:if test ="${formBean.feature.featureAssay.mutagen == 'spontaneous'}">
                    ${formBean.feature.featureAssay.mutagen}
                </c:if>
            </c:when>
            <c:when test="${formBean.feature.featureAssay.mutagen=='spontaneous'}" >
                ${formBean.feature.featureAssay.mutagen}
            </c:when>

            <c:otherwise>
                ${formBean.feature.featureAssay.mutagee}  treated with     ${formBean.feature.featureAssay.mutagen}
            </c:otherwise>
        </c:choose>

    </td>
</tr>



<tr>
    <td width="180">
        <b>Lab Of Origin:</b>
    </td>

    <c:choose>
    <c:when test="${formBean.feature.sources ne null && fn:length(formBean.feature.sources) > 0}">

    <c:forEach var="source" items="${formBean.feature.sources}" varStatus="status">

    <td>

        <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${source.organization.zdbID}">
                ${source.organization.name}
        </a>

        </c:forEach>

        </c:when>
        </c:choose>
<tr>
    <td width="180">
        <b>Map:</b>
    </td>



    <c:choose>

        <c:when test="${!empty formBean.featureMap}">
            <td>
                LG:
                <c:forEach var="lg" items="${formBean.featureMap}" varStatus="index">

                    <c:if test="${lg != 0}">

                        ${lg}
                    </c:if>
                    <c:if test="${!index.last}">
                        <c:if test="${lg != 0}">
                            ,&nbsp;
                        </c:if>
                    </c:if>
                </c:forEach>
                &nbsp;<a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-mappingdetail.apg&OID=${formBean.feature.zdbID}">Details</a>

            </td>


        </c:when>



        <c:when test="${!empty formBean.featureLocations}">
            <td>
                LG:
                <c:forEach var="lg" items="${formBean.featureLocations}" varStatus="index">
                    <c:if test="${lg != 0}">

                        ${lg}
                    </c:if>
                    <c:if test="${!index.last}">
                        ,&nbsp;
                    </c:if>
                </c:forEach>
                &nbsp;<a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-mappingdetail.apg&OID=${formBean.feature.zdbID}">Details</a>

            </td>


        </c:when>




        <c:otherwise>

            <td>None submitted</td>

        </c:otherwise>
    </c:choose>


</tr>
<c:if test="${formBean.feature.dbLinks != null && fn:length(formBean.feature.dbLinks) > 0}">
    <tr>
        <td>
            <b> Sequence: </b>
        </td>
        <td>
            <c:forEach var="featureGenbank" items="${formBean.feature.dbLinks}" varStatus="loop">
                <%--${featureGenbank.accessionNumber}--%>
                <zfin:link entity="${featureGenbank}"/>
                <c:if test="${featureGenbank.publicationCount > 0}">
                    <c:choose>
                        <c:when test="${featureGenbank.publicationCount == 1}">
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${featureGenbank.singlePublication.zdbID}">${featureGenbank.publicationCount}</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${featureGenbank.zdbID}">${featureGenbank.publicationCount}</a>)
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${!loop.last}">,&nbsp;</c:if>
            </c:forEach>
        </td>
    </tr>
</c:if>

<tr>
    <td width="150" valign="top">
        <c:choose>
            <c:when test="${fn:length(formBean.feature.suppliers) ne null && fn:length(formBean.feature.suppliers) > 1}">
                <b>Current Sources:</b>
            </c:when>
            <c:otherwise>
                <b>Current Source:</b>
            </c:otherwise>
        </c:choose>
    </td><td valign="top">
    <c:choose>
        <c:when test="${formBean.feature.suppliers ne null && fn:length(formBean.feature.suppliers) > 0}">
            <c:forEach var="supplier" items="${formBean.feature.suppliers}" varStatus="status">
                <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${supplier.organization.zdbID}" id="${supplier.organization.zdbID}">
                        ${supplier.organization.name}</a>
                <c:if test="${supplier.zirc}">&nbsp;(<a href="http://zebrafish.org/zirc/fish/lineAll.php?Allele=${formBean.feature.name}"><font size="-1">order this</font></a>)
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
            No data available
        </c:otherwise>
    </c:choose>
</td></tr>

<zfin2:notes hasNotes="${formBean.feature}" inTable="false"/>

</table>

<p/>
<c:choose>
    <c:when test="${fn:length(formBean.featgenoStats) > 0 }">
        <div id="short-version" class="summary">
            <div class="summaryTitle">GENOTYPES:</div>
            <table class="summary rowstripes">
                <tbody>
                <tr>
                    <th width="20%">
                        Genotype (Background)
                    </th>
                    <th width="20%">
                        Affected Genes
                    </th>
                    <th width="20%">
                        Phenotype
                    </th>
                    <th width="20%">
                        Gene Expression
                    </th>
                </tr>
                <c:forEach var="featgenoStat" items="${formBean.featgenoStats}" varStatus="loop" end="4">
                    <zfin:alternating-tr loopName="loop">
                        <td>
                            <zfin:link entity="${featgenoStat.genotype}"/>

                            <c:if test="${fn:length(featgenoStat.genotype.associatedGenotypes)>0}">
                                (<zfin:link entity="${featgenoStat.genotype.associatedGenotypes}"/>)

                            </c:if>
                        </td>
                        <td>
                            <zfin:link entity="${featgenoStat.affectedMarkers}"/>
                        </td>



                        <td>
                            <c:if test="${featgenoStat.numberOfFigures > 0}">
                                <c:if test="${featgenoStat.numberOfFigures > 1}">
                                    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${featgenoStat.genotype.zdbID}&includingMO=yes&split=yes'>
                                        <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                     integerEntity="${featgenoStat.numberOfFigures}" includeNumber="true"/></a>
                                </c:if>
                                <c:if test="${featgenoStat.numberOfFigures == 1 }">
                                    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.figure.zdbID}'>
                                        <zfin2:figureOrTextOnlyLink figure="${featgenoStat.figure}"
                                                                    integerEntity="${featgenoStat.numberOfFigures}"/>
                                    </a>
                                </c:if>
                            </c:if>
                            <c:if test="${featgenoStat.numberOfFigures == 0}">
                                &nbsp;
                            </c:if>

                            <c:if test="${featgenoStat.numberOfPublications ==1}">
                                from
                                <zfin:link entity="${featgenoStat.singlePublication}"/>
                            </c:if>
                            <c:if test="${featgenoStat.numberOfPublications > 1}">
                                from
                                <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                             integerEntity="${featgenoStat.numberOfPublications}"
                                             includeNumber="true"/>
                            </c:if>
                            <c:if test="${featgenoStat.isImage}">
                                <img src="/images/camera_icon.gif" alt="with image" image="" border="0">
                            </c:if>
                            <c:if test="${featgenoStat.isMorpholino}">
                                <img src="/images/MO_icon.gif" alt="MO">
                            </c:if>

                        </td>

                        <td>
                            <c:if test="${featgenoStat.numberOfExpFigures > 0}">
                                <c:if test="${featgenoStat.numberOfExpFigures > 1}">
                                    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-xpatselect.apg&query_results=true&mutsearchtype=equals&mutant_id=${featgenoStat.genotype.zdbID}'>
                                        <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                     integerEntity="${featgenoStat.numberOfExpFigures}" includeNumber="true"/></a>
                                </c:if>
                                <c:if test="${featgenoStat.numberOfExpFigures == 1 }">
                                    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.expFigure.zdbID}'>
                                        <zfin2:figureOrTextOnlyLink figure="${featgenoStat.expFigure}"
                                                                    integerEntity="${featgenoStat.numberOfExpFigures}"/>
                                    </a>
                                </c:if>
                            </c:if>

                            <c:if test="${featgenoStat.numberOfExpFigures == 0}">
                                &nbsp;
                            </c:if>

                            <c:if test="${featgenoStat.numberOfExpPublications ==1}">
                                from
                                <zfin:link entity="${featgenoStat.singleExpPublication}"/>
                            </c:if>
                            <c:if test="${featgenoStat.numberOfExpPublications > 1}">
                                from
                                <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                             integerEntity="${featgenoStat.numberOfExpPublications}"
                                             includeNumber="true"/>
                            </c:if>
                            <c:if test="${featgenoStat.isImageExp}">
                                <img src="/images/camera_icon.gif" alt="with image" image="" border="0">
                            </c:if>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>
                </tbody>
            </table>
            <c:if test="${fn:length(formBean.featgenoStats) > 5}">
                <div>
                    <br/>&nbsp;&nbsp;
                    <a href="javascript:expand()">
                        <img src="/images/darrow.gif" alt="expand" border="0">
                        Show all</a>
                        ${fn:length(formBean.featgenoStats)} genotypes
                </div>
            </c:if>
        </div>
    </c:when>
    <c:otherwise>
        <br>No data available</br>

    </c:otherwise>
</c:choose>


<div style="display:none" id="long-version" class="summary">
    <div class="summaryTitle">GENOTYPES:</div>
    <table class="summary rowstripes">
        <tbody>
        <tr class="search-result-table-header">
            <th width="20%">
                Genotype (Background)
            </th>
            <th width="20%">
                Affected Genes
            </th>
            <th width="20%">
                Phenotype
            </th>
            <th width="20%">
                Gene Expression
            </th>
        </tr>
        <c:forEach var="featgenoStat" items="${formBean.featgenoStats}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${featgenoStat.genotype}"/>

                    <c:if test="${fn:length(featgenoStat.genotype.associatedGenotypes)>0}">
                        (<zfin:link entity="${featgenoStat.genotype.associatedGenotypes}"/>)

                    </c:if>

                </td>
                <td>
                    <zfin:link entity="${featgenoStat.affectedMarkers}"/>
                </td>



                <td>
                    <c:if test="${featgenoStat.numberOfFigures > 0}">
                        <c:if test="${featgenoStat.numberOfFigures > 1}">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${featgenoStat.genotype.zdbID}&includingMO=yes&split=yes'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${featgenoStat.numberOfFigures}" includeNumber="true"/></a>
                        </c:if>
                        <c:if test="${featgenoStat.numberOfFigures == 1 }">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.figure.zdbID}'>
                                <zfin2:figureOrTextOnlyLink figure="${featgenoStat.figure}"
                                                            integerEntity="${featgenoStat.numberOfFigures}"/>
                            </a>
                        </c:if>
                    </c:if>
                    <c:if test="${featgenoStat.numberOfFigures == 0}">
                       &nbsp;
                    </c:if>

                    <c:if test="${featgenoStat.numberOfPublications ==1}">
                        from
                        <zfin:link entity="${featgenoStat.singlePublication}"/>
                    </c:if>
                    <c:if test="${featgenoStat.numberOfPublications > 1}">
                        from
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${featgenoStat.numberOfPublications}"
                                     includeNumber="true"/>
                    </c:if>
                    <c:if test="${featgenoStat.isMorpholino}">
                        <img src="/images/MO_icon.gif" alt="MO">
                    </c:if>
                    <c:if test="${featgenoStat.isImage}">
                        <img src="/images/camera_icon.gif" alt="with image" image="" border="0">
                    </c:if>

                </td>

                <td>
                    <c:if test="${featgenoStat.numberOfExpFigures > 0}">
                        <c:if test="${featgenoStat.numberOfExpFigures > 1}">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-xpatselect.apg&query_results=true&mutsearchtype=equals&mutant_id=${featgenoStat.genotype.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${featgenoStat.numberOfExpFigures}" includeNumber="true"/></a>
                        </c:if>
                        <c:if test="${featgenoStat.numberOfExpFigures == 1 }">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.expFigure.zdbID}'>
                                <zfin2:figureOrTextOnlyLink figure="${featgenoStat.expFigure}"
                                                            integerEntity="${featgenoStat.numberOfExpFigures}"/>
                            </a>
                        </c:if>
                    </c:if>
                    <c:if test="${featgenoStat.numberOfExpFigures == 0}">
                        &nbsp;
                    </c:if>

                    <c:if test="${featgenoStat.numberOfExpPublications ==1}">
                        from
                        <zfin:link entity="${featgenoStat.singleExpPublication}"/>
                    </c:if>
                    <c:if test="${featgenoStat.numberOfExpPublications > 1}">
                        from
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${featgenoStat.numberOfExpPublications}"
                                     includeNumber="true"/>
                    </c:if>

                    <c:if test="${featgenoStat.isImageExp}">
                        <img src="/images/camera_icon.gif" alt="with image" image="" border="0">
                    </c:if>

                </td>
            </zfin:alternating-tr>
        </c:forEach>





        </tbody>
    </table>
    <div>
        <a href="javascript:collapse()">
            <img src="/images/up.gif" alt="expand" title="Show first 5 genotypes" border="0">
            Show first</a> 5 genotypes
    </div>
</div>


<hr width="80%">
<a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${formBean.feature.zdbID}&total_count=${formBean.numPubs}'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.numPubs})

<script type="text/javascript">
    function expand() {
        document.getElementById('short-version').style.display = 'none';
        document.getElementById('long-version').style.display = 'inline';
    }

    function collapse() {
        document.getElementById('short-version').style.display = 'inline';
        document.getElementById('long-version').style.display = 'none';
    }
</script>
