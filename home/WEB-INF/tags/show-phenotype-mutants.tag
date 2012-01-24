<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.anatomy.presentation.AnatomySearchBean" required="true" %>

<div class="summary">
<div class="summaryTitle">Mutant and Transgenic Lines</div>

<c:if test="${formBean.mutantsExist}">
    <table class="summary rowstripes">
        <tbody>
        <tr>
            <th width="15%">
                Genotype (Background)
            </th>
            <th width="15%">
                Affected Genes
            </th>
            <th width="50%">
                Phenotype
            </th>
            <th width="20%">
                Figures
            </th>
        </tr>
        <c:forEach var="genoStat" items="${formBean.genotypeStatistics}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${genoStat.genotype}"/>
                    <c:if test="${fn:length(genoStat.genotype.associatedGenotypes)>0}">
                    </c:if>

                </td>
                <td>
                    <zfin:link entity="${genoStat.affectedMarkers}"/>
                </td>
                <td>
                    <c:forEach var="statement" items="${genoStat.phenotypeStatements}" varStatus="loop">
                        <zfin:link entity="${statement}"/>  <c:if test="${!loop.last}"><br/></c:if>
                    </c:forEach>
                </td>
                <td>
                    <c:if test="${genoStat.numberOfFigures > 0}">
                        <c:if test="${genoStat.numberOfFigures > 1}">
                            <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() %>?MIval=aa-pheno_summary.apg&OID=${genoStat.genotype.zdbID}&anatID=${formBean.aoTerm.zdbID}">
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${genoStat.numberOfFigures}" includeNumber="true"/></a>
                        </c:if>
                        <c:if test="${genoStat.numberOfFigures == 1 }">
                            <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() %>?MIval=aa-fxfigureview.apg&OID=${genoStat.figure.zdbID}">
                                <zfin2:figureOrTextOnlyLink figure="${genoStat.figure}"
                                                            integerEntity="${genoStat.numberOfFigures}"/>
                            </a>
                        </c:if>
                    </c:if>
                    <c:if test="${genoStat.numberOfFigures == 0}">
                        --
                    </c:if>
                    <zfin2:showCameraIcon hasImage="${genoStat.imgInFigure}"/> from
                    <c:if test="${genoStat.numberOfPublications ==1}">
                        <zfin:link entity="${genoStat.singlePublication}"/>
                    </c:if>
                    <c:if test="${genoStat.numberOfPublications > 1}">
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${genoStat.numberOfPublications}"
                                     includeNumber="true"/>
                    </c:if>
                    <c:if test="${genoStat.numberOfFigures == 0}">
                        --
                    </c:if>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>
</div>