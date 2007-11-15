<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width="100%">
    <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Gene Symbol
            </TD>
            <TD width="20%">
                Reagent
            </TD>
            <TD width="60%">
                Figures
            </TD>
        </TR>
        <c:if test="${!formBean.morpholinoExist}">
            <tr>
                <td colspan="3">No data available</td>
            </tr>
        </c:if>
        <c:forEach var="morpholinoStat" items="${formBean.allMorpholinos}">
            <tr class="search-result-table-entries">
                <td>
                    <zfin:link entity="${morpholinoStat.morpholino.targetGene}"/>
                </td>
                <td>
                    <zfin:link entity="${morpholinoStat.morpholino}"/>
                </td>
                <td>
                    --
<%--
Enable when the mutant search form is fixed to show morpholinos as well.

                    <c:if test="${morpholinoStat.numberOfFigures > 0}">
                        <c:if test="${morpholinoStat.numberOfFigures > 1}">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pheno_summary.apg&OID=${morpholinoStat.morpholino.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${morpholinoStat.numberOfFigures}" includeNumber="true"/>
                            </a>
                        </c:if>
                        <c:if test="${morpholinoStat.numberOfFigures == 1 }">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${morpholinoStat.figure.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${morpholinoStat.numberOfFigures}" includeNumber="true"/>
                            </a>
                        </c:if>
                        from
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${morpholinoStat.numberOfPublications}" includeNumber="true"/>
                    </c:if>
                    <c:if test="${morpholinoStat.numberOfFigures == 0}">
                        --
                    </c:if>
--%>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
