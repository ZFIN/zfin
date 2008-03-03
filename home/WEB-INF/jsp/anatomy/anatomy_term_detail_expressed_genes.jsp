<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<TABLE width="100%">
    <tbody>
        <TR class="search-result-table-header">
            <TD width="40%" colspan="2">
                Gene Symbol
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
                                                    useGeneZdbID="true"/>
                        </c:if>
                        <c:if test="${expressedGene.markerStat.numberOfFigures == 1 }">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${expressedGene.markerStat.figure.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${expressedGene.markerStat.numberOfFigures}"
                                             includeNumber="true"/>
                            </a>
                        </c:if>
                    </c:if>
                    from
                    <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                 integerEntity="${expressedGene.markerStat.numberOfPublications}" includeNumber="true"/>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${!formBean.expressedGenesExist}">
            <tr>
                <td colspan="4">No data available</td>
            </tr>
        </c:if>
        <c:if test="${formBean.expressedGenesExist}">
            <tr>
                <td colspan="4" align="left">
                    Show all
                    <a href='/<c:out value="${formBean.expressionSearchLink}"/>'>
                        <zfin:choice choicePattern="0#genes| 1#gene| 2#genes"
                                     integerEntity="${formBean.totalNumberOfExpressedGenes}" includeNumber="true"/>,
                        <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                     integerEntity="${formBean.totalNumberOfFiguresPerAnatomyItem}"
                                     includeNumber="true"/> </a>
                    &nbsp;
                    (including substructures
                    <a href='/${formBean.expressionSearchLinkSubstructures}'>
                        <zfin:choice choicePattern="0#genes| 1#gene| 2#genes"
                                     integerEntity="${formBean.anatomyStatistics.numberOfTotalDistinctObjects}"
                                     includeNumber="true"/>
                    </a>)
                </td>
            </tr>
            <tr>
                <td colspan="4" align="left ">
                </td>
            </tr>
        </c:if>
    </tbody>
</TABLE>
