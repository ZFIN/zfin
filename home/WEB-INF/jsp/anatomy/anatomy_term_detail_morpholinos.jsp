<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width="100%">
    <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Target Genes
            </TD>
            <TD width="20%">
                Morpholinos
            </TD>
            <TD width="20%">
                Genotype
            </TD>
            <TD width="20%">
                Phenotype
            </TD>
            <TD width="20%">
                Figures
            </TD>
        </TR>
        <c:if test="${!formBean.morpholinoExist}">
            <tr>
                <td colspan="3">No data available</td>
            </tr>
        </c:if>
        <c:forEach var="morpholinoStat" items="${formBean.allMorpholinos}">
            <tr class="search-result-table-entries" valign="top">
                <td>
                    <zfin:link entity="${morpholinoStat.morpholinoMarkers}"/>
                </td>
                <td>
                    <zfin:link entity="${morpholinoStat.genoExperiment.experiment.morpholinoConditions}"/>
                </td>
                <td>
                    <zfin:link entity="${morpholinoStat.genoExperiment.genotype}"/>
                </td>
                <td>
                    <c:forEach var="phenotypes" items="${morpholinoStat.phenotypeDescriptions}"
                               varStatus="ontologyLoop">
                    <c:if test="${phenotypes.key != 'ANATOMY'}">
                    [${phenotypes.key}]:
                    <div style="margin-left:20px">
                        </c:if>
                        <c:if test="${phenotypes.key == 'ANATOMY'}">
                        <div style="margin-left:0px">
                            </c:if>
                            <c:forEach var="phenotype" items="${phenotypes.value}" varStatus="loop">
                                ${phenotype}<c:if test="${!loop.last}">, </c:if>
                            </c:forEach>
                            <c:if test="${!ontologyLoop.last}">
                        </div>
                        </c:if>
                        </c:forEach>
                </td>
                <td>
                    <c:if test="${morpholinoStat.numberOfFigures > 0}">
                        <c:if test="${morpholinoStat.numberOfFigures > 1}">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pheno_summary.apg&OID=${morpholinoStat.genoExperiment.genotype.zdbID}&anatID=${formBean.anatomyItem.zdbID}&envID=${morpholinoStat.genoExperiment.experiment.zdbID}'>
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
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<c:if test="${!formBean.allWildtypeMorpholinosAreDisplayed}">
    <table width="100%">
        <tbody>
            <tr align="left">
                <td>
                    Show all
                    <a href="show-all-morpholino-experiments?anatomyItem.zdbID=${formBean.anatomyItem.zdbID}&wildtype=true">
                            ${formBean.wildtypeMorpholinoCount}
                        <zfin:choice choicePattern="0# experiments| 1# experiment| 2# experiments"
                                     integerEntity="${formBean.wildtypeMorpholinoCount}"/>
                    </a>
                </td>
            </tr>
        </tbody>
    </table>
</c:if>