<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<div class="summary">
    <b>Morpholino Experiments in Mutant and Transgenic Fish</b>

    <c:if test="${!formBean.nonWildtypeMorpholinoExist}">
        </br>No data available
    </c:if>
    <c:if test="${formBean.nonWildtypeMorpholinoExist}">
        <table class="summary rowstripes">
            <tbody>
            <tr>
                <th width="15%">
                    Target Genes
                </th>
                <th width="15%">
                    Morpholinos
                </th>
                <th width="20%">
                    Genotype
                </th>
                <th width="30%">
                    Phenotype
                </th>
                <th width="20%">
                    Figures
                </th>
            </tr>
            <c:forEach var="morpholinoStat" items="${formBean.nonWildtypeMorpholinos}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
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
                        <c:forEach var="statement" items="${morpholinoStat.phenotypeStatements}"
                                   varStatus="loop">
                            <zfin:link entity="${statement}"/> <c:if test="${!loop.last}"><br/></c:if>
                        </c:forEach>
                    </td>
                    <td>
                        <c:if test="${morpholinoStat.numberOfFigures > 0}">
                            <c:if test="${morpholinoStat.numberOfFigures > 1}">
                                <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${morpholinoStat.genoExperiment.genotype.zdbID}&anatID=${formBean.aoTerm.zdbID}&envID=${morpholinoStat.genoExperiment.experiment.zdbID}'>
                                    <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${morpholinoStat.numberOfFigures}"
                                                 includeNumber="true"/>
                                </a>
                            </c:if>
                            <c:if test="${morpholinoStat.numberOfFigures == 1 }">
                                <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${morpholinoStat.figure.zdbID}'>
                                    <zfin2:figureOrTextOnlyLink figure="${morpholinoStat.figure}"
                                                                integerEntity="${morpholinoStat.numberOfFigures}"/>
                                </a>
                            </c:if>
                        </c:if>
                        <c:if test="${morpholinoStat.numberOfFigures == 0}">
                            --
                        </c:if>
                        <zfin2:showCameraIcon hasImage="${morpholinoStat.imgInFigure}"/> from
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
                </zfin:alternating-tr>
            </c:forEach>
            </tbody>
        </table>
        <c:if test="${!formBean.allMutantMorpholinosAreDisplayed}">
            <table width="100%">
                <tbody>
                <tr align="left">
                    <td>
                        Show all
                        <a href="/action/anatomy/show-all-morpholinos/${formBean.aoTerm.zdbID}/false">
                                ${formBean.mutantMorpholinoCount}
                                <zfin:choice choicePattern="0# experiments| 1# experiment| 2# experiments"
                                             integerEntity="${formBean.mutantMorpholinoCount}"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </c:if>
    </c:if>
</div>