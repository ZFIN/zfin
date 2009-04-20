<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<b>
    <span title="Genes with Most Figures, annotated to ${formBean.anatomyItem.name}, substructures excluded">
        Genes with Most Figures
    </span>
</b>
<c:if test="${formBean.expressedGenesExist}">
    <TABLE width="100%">
        <tbody>
        <TR class="search-result-table-header">
            <TD width="40%" colspan="2">
                Gene
            </TD>
            <TD width="60%">
                Figures
            </TD>
        </TR>
        <c:forEach var="expressedGene" items="${formBean.allExpressedMarkers}">
            <tr class="search-result-table-entries">
                <td colspan="2">
                    <zfin:link entity="${expressedGene.markerStat.gene}"/>
                </td>
                <td>
                    <c:if test="${expressedGene.markerStat.numberOfFigures > 0}">
                        <c:if test="${expressedGene.markerStat.numberOfFigures > 1}">
                            <zfin:createFiguresLink marker="${expressedGene.markerStat.gene}"
                                                    anatomyItem="${formBean.anatomyItem}"
                                                    numberOfFigures="${expressedGene.markerStat.numberOfFigures}"
                                                    wildtypeOnly="true"
                                                    useGeneZdbID="true"/>
                        </c:if>
                        <c:if test="${expressedGene.markerStat.numberOfFigures == 1 }">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${expressedGene.markerStat.figure.zdbID}'>
                                <zfin2:figureOrTextOnlyLink figure="${expressedGene.markerStat.figure}"
                                                            integerEntity="${expressedGene.markerStat.numberOfFigures}"/>
                            </a>
                        </c:if>
                    </c:if>
                    from
                    <c:if test="${expressedGene.markerStat.numberOfPublications ==1}">
                        <zfin:link entity="${expressedGene.markerStat.singlePublication}"/>
                    </c:if>
                    <c:if test="${expressedGene.markerStat.numberOfPublications > 1}">
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${expressedGene.markerStat.numberOfPublications}"
                                     includeNumber="true"/>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </TABLE>
</c:if>
<zfin2:anatomyTermDetailSectionCaption anatomyItem="${formBean.anatomyItem}"
                                       recordsExist="${formBean.expressedGenesExist}"
                                       anatomyStatistics="${formBean.anatomyStatistics}"
                                       structureSearchLink="/${formBean.expressionSearchLink}"
                                       substructureSearchLink="/${formBean.expressionSearchLinkSubstructures}"
                                       choicePattern="0# genes| 1# gene| 2# genes"
                                       allRecordsAreDisplayed="${formBean.allExpressedGenesAreDisplayed}"
                                       totalRecordCount="${formBean.totalNumberOfExpressedGenes}"
                                       displayImages="true"
                                       imageCount="${formBean.totalNumberOfFiguresPerAnatomyItem}"/>
