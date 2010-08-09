<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<b>Morpholino Experiments in Mutant and Transgenic Fish</b>

<c:if test="${!formBean.nonWildtypeMorpholinoExist}">
    </br>No data available
</c:if>
<c:if test="${formBean.nonWildtypeMorpholinoExist}">
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
            <c:forEach var="morpholinoStat" items="${formBean.nonWildtypeMorpholinos}">
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
                                <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${morpholinoStat.genoExperiment.genotype.zdbID}&anatID=${formBean.aoTerm.ID}&envID=${morpholinoStat.genoExperiment.experiment.zdbID}'>
                                    <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${morpholinoStat.numberOfFigures}"
                                                 includeNumber="true"/>
                                </a>
                            </c:if>
                            <c:if test="${morpholinoStat.numberOfFigures == 1 }">
                                <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${morpholinoStat.figure.zdbID}'>
                                    <zfin2:figureOrTextOnlyLink figure="${morpholinoStat.figure}" integerEntity="${morpholinoStat.numberOfFigures}"/>
                                </a>
                            </c:if>
                        </c:if>
                        <c:if test="${morpholinoStat.numberOfFigures == 0}">
                            --
                        </c:if>
                        from
                        <c:if test="${morpholinoStat.numberOfPublications ==1}">
                            <zfin:link entity="${morpholinoStat.singlePublication}"/>
                        </c:if>
                        <c:if test="${morpholinoStat.numberOfPublications > 1}">
                            <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                         integerEntity="${morpholinoStat.numberOfPublications}"
                                         includeNumber="true"/>
                        </c:if>
                    <c:if test="${morpholinoStat.numberOfFigures == 0}">
                        --
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
    <c:if test="${!formBean.allMutantMorpholinosAreDisplayed}">
        <table width="100%">
            <tbody>
                <tr align="left">
                    <td>
                        Show all
                        <a href="show-all-morpholino-experiments?aoTerm.ID=${formBean.aoTerm.ID}&wildtype=false">
                                ${formBean.mutantMorpholinoCount}
                                <zfin:choice choicePattern="0# experiments| 1# experiment| 2# experiments"
                                             integerEntity="${formBean.mutantMorpholinoCount}"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:if>
</c:if>
