<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="data-sub-page-title">Genotype Expression Figure Summary</div>
<div style="float: right">

    <c:choose>
        <c:when test="${!empty expressionCriteria.genotypeExperiment}">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">                
                <tiles:putAttribute name="subjectName" value="${expressionCriteria.genotypeExperiment.genotype.name}"/>
                <tiles:putAttribute name="subjectID" value="${expressionCriteria.genotypeExperiment.genotype.zdbID}"/>
            </tiles:insertTemplate>
        </c:when>
        <c:when test="${!empty expressionCriteria.genotype}">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="${expressionCriteria.genotype.name}"/>
                <tiles:putAttribute name="subjectID" value="${expressionCriteria.genotype.zdbID}"/>
            </tiles:insertTemplate>
        </c:when>
    </c:choose>



</div>

<zfin2:expressionSummaryCriteria criteria="${expressionCriteria}"/>

<div class="summary">
  <span class="summaryTitle">Genotype Expression</span>
  <zfin2:figureSummary figureSummaryDisplayList="${figureSummaryDisplayList}"/>
</div>