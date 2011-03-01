<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<b>Antibodies</b>

<c:if test="${formBean.antibodiesExist}">
    <TABLE width="100%">
        <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Antibody
            </TD>
            <TD width="20%">
                Gene
            </TD>
            <TD width="60%">
                Figures
            </TD>
        </TR>
        <c:forEach var="antibodyStats" items="${formBean.antibodyStatistics}">
            <tr class="search-result-table-entries">
                <td>
                    <zfin:link entity="${antibodyStats.antibody}"/>
                </td>
                <td>
                    <zfin:link entity="${antibodyStats.genes}"/>
                </td>
                <td>
                    <c:if test="${antibodyStats.numberOfFigures > 0}">
                        <!-- link to figure search page if more than one figure available-->
                        <c:if test="${antibodyStats.numberOfFigures > 1}">
                            <a href='/action/antibody/figure-summary?superTerm.ID=${formBean.aoTerm.zdbID}&antibody.zdbID=${antibodyStats.antibody.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${antibodyStats.numberOfFigures}"
                                             includeNumber="true"/>
                            </a>
                        </c:if>
                        <!-- If only one figure available go directly to the figure page -->
                        <c:if test="${antibodyStats.numberOfFigures == 1}">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${antibodyStats.figure.zdbID}'>
                                <zfin2:figureOrTextOnlyLink figure="${antibodyStats.figure}"
                                                            integerEntity="${antibodyStats.numberOfFigures}"/>
                            </a>
                        </c:if>
                        from
                        <c:if test="${antibodyStats.numberOfPubs ==1}">
                            <zfin:link entity="${antibodyStats.singlePub}"/>
                        </c:if>
                        <c:if test="${antibodyStats.numberOfPubs > 1}">
                            <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                         integerEntity="${antibodyStats.numberOfPubs}"
                                         includeNumber="true"/>
                        </c:if>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </TABLE>
</c:if>
<zfin2:anatomyTermDetailSectionCaption anatomyItem="${formBean.aoTerm}"
                                       recordsExist="${formBean.antibodiesExist}"
                                       anatomyStatistics="${formBean.anatomyStatisticsAntibodies}"
                                       structureSearchLink="/action/antibody/search?antibodyCriteria.includeSubstructures=false&antibodyCriteria.anatomyTermNames=${formBean.aoTerm.termName}&antibodyCriteria.anatomyTermIDs=${formBean.aoTerm.zdbID}&action=SEARCH"
                                       substructureSearchLink="/action/antibody/search?antibodyCriteria.includeSubstructures=true&antibodyCriteria.anatomyTermNames=${formBean.aoTerm.termName}&antibodyCriteria.anatomyTermIDs=${formBean.aoTerm.zdbID}&action=SEARCH"
                                       choicePattern="0# antibodies| 1# antibody| 2# antibodies"
                                       allRecordsAreDisplayed="${formBean.allAntibodiesAreDisplayed}"
                                       totalRecordCount="${formBean.antibodyCount}"/>
