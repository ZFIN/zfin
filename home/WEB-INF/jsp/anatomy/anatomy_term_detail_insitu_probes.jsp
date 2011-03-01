<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<c:if test="${formBean.inSituProbesExist}">
    <b>In Situ Probes</b>: <a href="/zf_info/stars.html"> Recommended </a> by
    <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-labview.apg&OID=ZDB-LAB-980204-15'>
        Thisse lab</a>

    <TABLE width="100%">
        <tbody>
            <TR class="search-result-table-header">
                <TD width="20%">
                    Gene
                </TD>
                <TD width="20%">
                    Probe
                </TD>
                <TD width="60%">
                    Figures
                </TD>
            </TR>
            <c:forEach var="probeStats" items="${formBean.highQualityProbeGenes}">
                <tr class="search-result-table-entries">
                    <td>
                        <zfin:link entity="${probeStats.genes}"/>
                    </td>
                    <td>
                        <zfin:link entity="${probeStats.probe}"/>
                    </td>
                    <td>
                        <c:if test="${probeStats.numberOfFigures > 0}">
                            <!-- link to figure search page if more than one figure available-->
                            <c:if test="${probeStats.numberOfFigures > 1}">
                                <zfin:createFiguresLink marker="${probeStats.probe}" term="${formBean.aoTerm}"
                                                        numberOfFigures="${probeStats.numberOfFigures}" author="Thisse"
                                                        useGeneZdbID="false"/>
                            </c:if>
                            <!-- If only one figure available go directly to the figure page -->
                            <c:if test="${probeStats.numberOfFigures == 1}">
                                <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${probeStats.figure.zdbID}'>
                                    <zfin2:figureOrTextOnlyLink figure="${probeStats.figure}"
                                                                integerEntity="${probeStats.numberOfFigures}"/>
                                </a>
                            </c:if>
                            from
                            <c:if test="${probeStats.numberOfPubs ==1}">
                                <zfin:link entity="${probeStats.singlePub}"/>
                            </c:if>
                            <c:if test="${probeStats.numberOfPubs > 1}">
                                <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                             integerEntity="${probeStats.numberOfPubs}"
                                             includeNumber="true"/>
                            </c:if>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </TABLE>
    <zfin2:anatomyTermDetailSectionCaption anatomyItem="${formBean.aoTerm}"
                                           recordsExist="${formBean.inSituProbesExist}"
                                           anatomyStatistics="${formBean.anatomyStatisticsProbe}"
                                           structureSearchLink="/action/anatomy/high-quality-probes?anatomyItem.zdbID=${formBean.aoTerm.zdbID}"
                                           choicePattern="0# Probes| 1# probe| 2# probes"
                                           allRecordsAreDisplayed="${formBean.allProbesAreDisplayed}"
                                           totalRecordCount="${formBean.numberOfHighQualityProbes}"/>
</c:if>  
