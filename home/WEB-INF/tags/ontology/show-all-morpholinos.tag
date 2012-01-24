<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.anatomy.presentation.AnatomySearchBean" required="true" %>
<%@ attribute name="wildtype" type="java.lang.Boolean" required="true" %>

<table border="0" width="100%">
    <tbody>
        <tr align="left">
            <td><b>All ${formBean.wildtypeMorpholinoCount} Morpholino Experiments
                (<c:if test="${wildtype}">wild-type</c:if><c:if test="${!wildtype}">mutant</c:if>)
             for:</b>
                <zfin:link entity="${formBean.aoTerm}" />
            </td>
        </tr>
    </tbody>
</table>

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
                Data
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
                    <zfin:link entity="${morpholinoStat.morpholinoMarkers}"/>
                </td>
                <td>
                    <zfin:link entity="${morpholinoStat.genoExperiment.experiment.morpholinoConditions}"/>
                </td>
                <td>
                    <zfin:link entity="${morpholinoStat.genoExperiment.genotype}"/>
                </td>
                <td>
                    <c:forEach var="statement" items="${morpholinoStat.phenotypeStatements}" varStatus="loop">
                        <zfin:link entity="${statement}"/> <c:if test="${!loop.last}"><br/></c:if>
                    </c:forEach>
                </td>
                <td>
                    <c:if test="${morpholinoStat.numberOfFigures > 0}">
                        <c:if test="${morpholinoStat.numberOfFigures > 1}">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${morpholinoStat.genoExperiment.genotype.zdbID}&anatID=${formBean.aoTerm.zdbID}&envID=${morpholinoStat.genoExperiment.experiment.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${morpholinoStat.numberOfFigures}" includeNumber="true"/>
                            </a>
                        </c:if>
                        <c:if test="${morpholinoStat.numberOfFigures == 1 }">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${morpholinoStat.figure.zdbID}'>
                                <zfin2:figureOrTextOnlyLink figure="${morpholinoStat.figure}"
                                                            integerEntity="${morpholinoStat.numberOfFigures}"/>
                            </a>
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
                    </c:if>
                    <c:if test="${morpholinoStat.numberOfFigures == 0}">
                        --
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<p/>
<zfin2:pagination paginationBean="${formBean}"/>
