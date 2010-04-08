<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>

<zfin2:dataManager zdbID="${formBean.feature.zdbID}"
                   <%--editURL="${formBean.editURL}"--%>
                   <%--deleteURL="${formBean.deleteURL}"--%>
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
        <tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:put name="subjectName" value="${formBean.feature.name}"/>
            <tiles:put name="subjectID" value="${formBean.feature.zdbID}"/>
        </tiles:insert>
    </td>
</tr>
<c:if test="${formBean.feature.featureType.dispName == 'unspecified'}">

    <br style="font-size:small;">    Note: Unspecified  genomic feature records have been created in support of data for which a publication has not specified a genomic feature. </br>

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
                            (<a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${featureAlias.singlePublication.zdbID}">${featureAlias.publicationCount}</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="alias-publication-list?featureAlias.zdbID=${featureAlias.zdbID}&orderBy=author">${featureAlias.publicationCount}</a>)
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
        <c:when test="${fn:length(formBean.featureStat.sortedMarkerRelationships) > 0 }">

            <td>
                <c:forEach var="fmRel" items="${formBean.featureStat.sortedMarkerRelationships}" varStatus="loop">
                    <zfin:link entity="${fmRel.marker}"/>

                    <c:if test="${fmRel.publicationCount > 0}">
                        <c:choose>
                            <c:when test="${fmRel.publicationCount == 1}">
                                (<a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${fmRel.singlePublication.zdbID}">${fmRel.publicationCount}</a>)
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
                <c:if test="${fn:length(formBean.featureStat.mappedDeletions) > 0 }">
               <c:forEach var="mapDel" items="${formBean.featureStat.mappedDeletions}" varStatus="loop">
                    , <zfin:link entity="${mapDel}"/>
                    <c:if test="${!loop.last}">
                        ,&nbsp;
                    </c:if>
                </c:forEach>
                </c:if>
            </td>
        </c:when>
        <c:when test="${fn:length(formBean.featureStat.mappedDeletions) > 0 }">

            <td>
                <c:forEach var="mapDel" items="${formBean.featureStat.mappedDeletions}" varStatus="loop">
                    <zfin:link entity="${mapDel}"/>
                    <c:if test="${!loop.last}">
                        ,&nbsp;
                    </c:if>
                </c:forEach>

            </td>
        </c:when>
        <c:otherwise>
            <c:if test="${formBean.feature.featureType.dispName == 'Transgenic Insertion'}">
                <td>
                    This feature is representative of one or more unknown insertion sites.
                </td>
            </c:if>
        </c:otherwise>
    </c:choose>
    <%--<td>
This feature is representative of one or more unknown insertion sites.
    </td>--%>

</tr>



<c:if test="${formBean.feature.featureType.dispName == 'Transgenic Insertion'}">
<tr>
    <td width="180">
        <b>Construct:</b>
    </td>
    <td>
        <c:forEach var="mRel" items="${formBean.featureStat.sortedConstructRelationships}" varStatus="loop">
            <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&OID=${mRel.marker.zdbID}">${mRel.marker.name}</a>
            <%--//<zfin:name entity="${mRel.marker}"/>--%>

            <c:if test="${mRel.publicationCount > 0}">
                <c:choose>
                    <c:when test="${mRel.publicationCount == 1}">
                        (<a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${mRel.singlePublication.zdbID}">${mRel.publicationCount}</a>)
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
    <td>  ${formBean.feature.featureType.dispName}
        <c:if test="${fn:length(formBean.featureStat.ftrTypeAttr) > 0 }">
         <c:choose>
                    <c:when test="${fn:length(formBean.featureStat.ftrTypeAttr)== 1 }">
                        (<a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${formBean.featureStat.singlePublication}">${1}</a>)
                    </c:when>
                    <c:otherwise>
                        (<a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-showpubs.apg&rtype=genotype&recattrsrctype=feature+type&OID=${formBean.feature.zdbID}">${fn:length(formBean.featureStat.ftrTypeAttr)}</a>)
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
            <c:when test="${formBean.feature.featureAssay.mutagen=='Not Specified'}" >
                Not Specified
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
                ${formBean.feature.featureAssay.mutagee}    treated with     ${formBean.feature.featureAssay.mutagen}
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

        <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-sourceview.apg&OID=${source.organization.zdbID}">
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


        <c:when test="${!empty formBean.mappedMarkerBean.unMappedMarkers}">
            <td>
                LG:
                <c:forEach var="lg" items="${formBean.mappedMarkerBean.unMappedMarkers}" varStatus="index">
                   <c:if test="${lg != 0}">
                        ${lg}
                      </c:if>
                     <c:if test="${!index.last && lg !=0 }">
                ,&nbsp;
            </c:if>
                </c:forEach>
                &nbsp;<a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-mappingdetail.apg&OID=${formBean.feature.zdbID}">Details</a>

            </td>

        </c:when>
        <c:when test="${!empty formBean.featureStat.ftrLocations}">
            <td>
                LG:
                <c:forEach var="lg" items="${formBean.featureStat.ftrLocations}" varStatus="index">
                    <c:if test="${lg != 0}">

                        ${lg}
                    </c:if>
                     <c:if test="${!index.last}">
                ,&nbsp;
            </c:if>
                </c:forEach>
                &nbsp;<a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-mappingdetail.apg&OID=${formBean.feature.zdbID}">Details</a>

            </td>


        </c:when>
        <c:when test="${!empty formBean.featureStat.ftrMap}">
                    <td>
                        LG:
                        <c:forEach var="lg" items="${formBean.featureStat.ftrMap}" varStatus="index">

                            <c:if test="${lg != 0}">

                                ${lg}
                            </c:if>
                             <c:if test="${!index.last}">
                        ,&nbsp;
                    </c:if>
                        </c:forEach>
                        &nbsp;<a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-mappingdetail.apg&OID=${formBean.feature.zdbID}">Details</a>

                    </td>


                </c:when>
         <c:when test="${!empty formBean.featureStat.ftrLinkage}">
                    <td>
                        LG:
                        <c:forEach var="lg" items="${formBean.featureStat.ftrLinkage}" varStatus="index">

                            <c:if test="${lg != 0}">

                                ${lg}
                            </c:if>
                             <c:if test="${!index.last}">
                        ,&nbsp;
                    </c:if>
                        </c:forEach>
                        &nbsp;<a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-mappingdetail.apg&OID=${formBean.feature.zdbID}">Details</a>

                    </td>


                </c:when>


        <c:otherwise>

            <td>None submitted</td>

        </c:otherwise>
    </c:choose>


</tr>


<tr>
    <td width="180">
        <b>Notes:</b>
    </td>
    <td>  ${formBean.feature.comments}

    </td>
</tr>
<tr><td></td></tr>
<tr><td></td></tr>
<tr><td></td></tr>
<tr><td></td></tr>
<authz:authorize ifAnyGranted="root">
    <tr>
        <td width="180">
            <b>Curator Notes:</b>
        </td>
         <td>

        <c:if test="${fn:length(formBean.featureStat.featureNote)>0}">
            <c:forEach var="datanote" items="${formBean.featureStat.featureNote}" varStatus="status">

                        ${datanote.note}
                 <c:if test="${!loop.last}">
                <br>
            </c:if>
            </c:forEach>
        </c:if>


    </tr>
</authz:authorize>

</table>

<p/>
<b>GENOTYPES:</b>
<c:choose>
    <c:when test="${fn:length(formBean.featgenoStats) > 0 }">
        <div id="short-version" class="summary">
            <table width="100%">
                <tbody>
                <TR class="search-result-table-header">
                    <TD width="20%">
                        Genotype (Background)
                    </TD>
                    <TD width="20%">
                        Affected Genes
                    </TD>
                    <TD width="20%">
                        Phenotype
                    </TD>
                    <TD width="20%">
                        Gene Expression
                    </TD>

                </TR>
                <c:forEach var="featgenoStat" items="${formBean.featgenoStats}" varStatus="loop" end="4">
                <tr class="search-result-table-entries">
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
                                <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pheno_summary.apg&OID=${featgenoStat.genotype.zdbID}&includingMO=yes&split=yes'>
                                    <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${featgenoStat.numberOfFigures}" includeNumber="true"/></a>
                            </c:if>
                            <c:if test="${featgenoStat.numberOfFigures == 1 }">
                                <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.figure.zdbID}'>
                                    <zfin2:figureOrTextOnlyLink figure="${featgenoStat.figure}"
                                                                integerEntity="${featgenoStat.numberOfFigures}"/>
                                </a>
                            </c:if>
                        </c:if>
                        <c:if test="${featgenoStat.numberOfFigures == 0}">
                            --
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
                                <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect.apg&query_results=true&mutsearchtype=equals&mutant_id=${featgenoStat.genotype.zdbID}'>
                                    <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${featgenoStat.numberOfExpFigures}" includeNumber="true"/></a>
                            </c:if>
                            <c:if test="${featgenoStat.numberOfExpFigures == 1 }">
                                <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.expFigure.zdbID}'>
                                    <zfin2:figureOrTextOnlyLink figure="${featgenoStat.expFigure}"
                                                                integerEntity="${featgenoStat.numberOfExpFigures}"/>
                                </a>
                            </c:if>
                        </c:if>

                        <c:if test="${featgenoStat.numberOfExpFigures == 0}">
                            --
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
                    </c:forEach>




                </tr>
                <tr>
                    <td>
                        <c:if test="${fn:length(formBean.featgenoStats) > 5}">
                            <br/>&nbsp;&nbsp;
                            <a href="javascript:expand()">
                                <img src="/images/darrow.gif" alt="expand" border="0">
                                Show all</a>
                            ${fn:length(formBean.featgenoStats)} genotypes
                        </c:if>
                    </td>
                </tr>




                </tbody>
            </table>
        </div>
    </c:when>
    <c:otherwise>
        <br>No data available</br>

    </c:otherwise>
</c:choose>

<div style="display:none" id="long-version" class="summary">
    <table width="100%">
        <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Genotype (Background)
            </TD>
            <TD width="20%">
                Affected Genes
            </TD>
            <TD width="20%">
                Phenotype
            </TD>
            <TD width="20%">
                Gene Expression
            </TD>

        </TR>
        <c:forEach var="featgenoStat" items="${formBean.featgenoStats}">
        <tr class="search-result-table-entries">
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
                        <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pheno_summary.apg&OID=${featgenoStat.genotype.zdbID}&includingMO=yes&split=yes'>
                            <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                         integerEntity="${featgenoStat.numberOfFigures}" includeNumber="true"/></a>
                    </c:if>
                    <c:if test="${featgenoStat.numberOfFigures == 1 }">
                        <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.figure.zdbID}'>
                            <zfin2:figureOrTextOnlyLink figure="${featgenoStat.figure}"
                                                        integerEntity="${featgenoStat.numberOfFigures}"/>
                        </a>
                    </c:if>
                </c:if>
                <c:if test="${featgenoStat.numberOfFigures == 0}">
                    --
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
                        <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect.apg&query_results=true&mutsearchtype=equals&mutant_id=${featgenoStat.genotype.zdbID}'>
                            <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                         integerEntity="${featgenoStat.numberOfExpFigures}" includeNumber="true"/></a>
                    </c:if>
                    <c:if test="${featgenoStat.numberOfExpFigures == 1 }">
                        <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.expFigure.zdbID}'>
                            <zfin2:figureOrTextOnlyLink figure="${featgenoStat.expFigure}"
                                                        integerEntity="${featgenoStat.numberOfExpFigures}"/>
                        </a>
                    </c:if>
                </c:if>
                <c:if test="${featgenoStat.numberOfExpFigures == 0}">
                    --
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
            </c:forEach>




        </tr>
        <tr>
            <td>
                <br/>&nbsp;&nbsp;
                <a href="javascript:collapse()">
                    <img src="/images/up.gif" alt="expand" title="Show first 5 genotypes" border="0">
                    Show first</a> 5 genotypes
            </td>
        </tr>




        </tbody>
    </table>
</div>


<hr width="80%">
<a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-showpubs.apg&OID=${formBean.feature.zdbID}'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.numPubs})

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
