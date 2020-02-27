<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="expressionCriteria" class="org.zfin.expression.ExpressionSummaryCriteria" scope="request"/>

<div class="data-sub-page-title">Expression Figure Summary</div>

<zfin2:expressionSummaryCriteria criteria="${expressionCriteria}"/>

<div class="summary">
    <c:if test="${!empty expressionCriteria.fish}"><span class="summaryTitle">Fish Expression</span></c:if>
    <c:if test="${!empty expressionCriteria.sequenceTargetingReagent}"><span class="summaryTitle">Expression</span></c:if>
    <zfin2:figureSummary figureSummaryList="${figureSummaryDisplayList}" expressionGenotypeData="true"/>
</div>