<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="data-sub-page-title">Expression Figure Summary</div>
<div style="float: right">

    <c:choose>
        <c:when test="${!empty expressionCriteria.fishExperiment}">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="${expressionCriteria.fishExperiment.fish.genotype.name}"/>
            </tiles:insertTemplate>
        </c:when>
        <c:when test="${!empty expressionCriteria.genotype}">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="${expressionCriteria.genotype.name}"/>
            </tiles:insertTemplate>
        </c:when>

        <c:when test="${!empty expressionCriteria.sequenceTargetingReagent}">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="${expressionCriteria.sequenceTargetingReagent.name}"/>
            </tiles:insertTemplate>
        </c:when>
    </c:choose>
</div>

<zfin2:expressionSummaryCriteria criteria="${expressionCriteria}"/>

<div class="summary">
    <c:if test="${!empty expressionCriteria.genotype}"><span class="summaryTitle">Genotype Expression</span></c:if>
    <c:if test="${!empty expressionCriteria.sequenceTargetingReagent}"><span class="summaryTitle">Sequence Targeting Reagent Expression</span></c:if>
    <zfin2:figureSummary figureExpressionSummaryList="${figureSummaryDisplayList}" expressionGenotypeData="true"/>
</div>