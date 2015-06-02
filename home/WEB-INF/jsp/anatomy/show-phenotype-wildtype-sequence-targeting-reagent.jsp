<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<div class="summary">
    <div class="summaryTitle">Knockdown Experiments in Wild-type Fish</div>
    <c:if test="${!formBean.sequenceTargetingReagentExist}">
        No data available
    </c:if>
    <c:if test="${formBean.sequenceTargetingReagentExist}">
        <table class="summary rowstripes">
            <tbody>
            <tr>
                <th width="15%">
                    Target Genes
                </th>
                <th width="30%">
                    Fish
                </th>
                <th width="30%">
                    Phenotype
                </th>
                <th width="25%">
                    Figures
                </th>
            </tr>
            <c:forEach var="sequenceTargetingReagentStat" items="${formBean.allSequenceTargetingReagents}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td>
                        <zfin:link entity="${sequenceTargetingReagentStat.sequenceTargetingReagents}"/>
                    </td>
                    <td>
                        <zfin:link entity="${sequenceTargetingReagentStat.fishExperiment.fish}"/>
                    </td>
                    <td>
                        <c:forEach var="statement" items="${sequenceTargetingReagentStat.phenotypeStatements}" varStatus="loop">
                            <zfin:link entity="${statement}"/> <c:if test="${!loop.last}"><br/></c:if>
                        </c:forEach>
                    </td>
                    <td>
                        <c:if test="${sequenceTargetingReagentStat.numberOfFigures > 0}">
                            <c:if test="${sequenceTargetingReagentStat.numberOfFigures > 1}">
                                <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() %>?MIval=aa-pheno_summary.apg&OID=${sequenceTargetingReagentStat.fishExperiment.fish.genotype.zdbID}&anatID=${formBean.aoTerm.zdbID}&envID=${sequenceTargetingReagentStat.fishExperiment.experiment.zdbID}">
                                    <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${sequenceTargetingReagentStat.numberOfFigures}"
                                                 includeNumber="true"/>
                                </a>
                            </c:if>
                            <c:if test="${sequenceTargetingReagentStat.numberOfFigures == 1 }">
                                <a href="/${sequenceTargetingReagentStat.figure.zdbID}">
                                    <zfin2:figureOrTextOnlyLink figure="${sequenceTargetingReagentStat.figure}"
                                                                integerEntity="${sequenceTargetingReagentStat.numberOfFigures}"/>
                                </a>
                            </c:if>
                        </c:if>
                        <c:if test="${sequenceTargetingReagentStat.numberOfFigures == 0}">
                            --
                        </c:if>
                        <zfin2:showCameraIcon hasImage="${sequenceTargetingReagentStat.imgInFigure}"/> from
                        <c:if test="${sequenceTargetingReagentStat.numberOfPublications ==1}">
                            <zfin:link entity="${sequenceTargetingReagentStat.singlePublication}"/>
                        </c:if>
                        <c:if test="${sequenceTargetingReagentStat.numberOfPublications > 1}">
                            <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                         integerEntity="${sequenceTargetingReagentStat.numberOfPublications}"
                                         includeNumber="true"/>
                        </c:if>
                        <c:if test="${sequenceTargetingReagentStat.numberOfFigures == 0}">
                            --
                        </c:if>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
            </tbody>
        </table>
        <c:if test="${!formBean.allWildtypeSTRsDisplayed}">
            <table width="100%">
                <tbody>
                <tr align="left">
                    <td>
                        Show all
                        <a href="/action/ontology/show-all-sequence-targeting-reagents/${formBean.aoTerm.zdbID}/true">
                            ${formBean.wildtypeSTRcount}
                            <zfin:choice choicePattern="0# experiments| 1# experiment| 2# experiments"
                                         integerEntity="${formBean.wildtypeSTRcount}"/>
                        </a>
                    </td>
                </tr>
                </tbody>
            </table>
        </c:if>
    </c:if>
</div>