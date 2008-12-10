<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${!formBean.expressedGenesExist}">
    <c:choose>
        <c:when test="${formBean.anatomyStatistics.numberOfTotalDistinctObjects > 0}">
            </br>No data for '${formBean.anatomyItem.name}'.
            Show all <a href='/${formBean.expressionSearchLinkSubstructures}'>
            <zfin:choice choicePattern="0#genes| 1#gene| 2#genes"
                         integerEntity="${formBean.anatomyStatistics.numberOfTotalDistinctObjects}"
                         includeNumber="true"/></a> in substructures.
        </c:when>
        <c:otherwise>
            </br>No data available.
        </c:otherwise>
    </c:choose>
</c:if>
<c:if test="${formBean.expressedGenesExist}">
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
    <c:choose>
        <c:when test="${!formBean.allExpressedGenesAreDisplayed}">
            <table width="100%">
                <tbody>
                <tr align="left">
                    <td>
                        Show all
                        <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fishselect.apg&fsel_anatomy_item_id=${formBean.anatomyItem.zdbID}&WINSIZE=20&include_substructures=unchecked">
                                ${formBean.expressedGeneCount}
                            <zfin:choice choicePattern="0# genes| 1# gene| 2# genes"
                                         integerEntity="${formBean.expressedGeneCount}"/></a> &nbsp;
                        <c:if test="${formBean.anatomyStatistics.numberOfTotalDistinctObjects > formBean.expressedGeneCount }">
                            (including substructures
                            <a href='/${formBean.expressionSearchLinkSubstructures}'>
                                <zfin:choice choicePattern="0#genes| 1#gene| 2#genes"
                                             integerEntity="${formBean.anatomyStatistics.numberOfTotalDistinctObjects}"
                                             includeNumber="true"/></a>)
                        </c:if>
                    </td>
                </tr>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <c:if test="${formBean.anatomyStatistics.numberOfTotalDistinctObjects > 0 &&
                          formBean.anatomyStatistics.numberOfTotalDistinctObjects > formBean.expressedGeneCount }">
                <table width="100%">
                    <tbody>
                    <tr align="left">
                        <td>
                            Show all
                            <a href='/${formBean.expressionSearchLinkSubstructures}'>
                                <zfin:choice choicePattern="0# genes| 1# gene| 2# genes"
                                             integerEntity="${formBean.anatomyStatistics.numberOfTotalDistinctObjects}"
                                             includeNumber="true"/></a> in substructures
                        </td>
                    </tr>
                    </tbody>
                </table>
            </c:if>
        </c:otherwise>
    </c:choose>
</c:if>