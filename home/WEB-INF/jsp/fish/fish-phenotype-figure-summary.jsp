<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="phenotypeSummaryCriteria" class="org.zfin.fish.presentation.PhenotypeSummaryCriteria" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td width="100%" class="titlebar">
            <span style="font-size: larger; font-weight: bold;">
            Phenotype Summary
                </span>

        <span style="float: right">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Phenotype summary from Fish search"/>
            </tiles:insertTemplate>
        </td>
    </tr>
</table>

<table class="primary-entity-attributes">
    <tr>
        <th>Fish:</th>
        <td>
            <zfin:link entity="${fish}"/>
        </td>
    </tr>
    <c:if test="${!empty phenotypeSummaryCriteria.fishExperiments}">
        <tr>
            <th>Conditions:</th>
            <td>
                <zfin:link entity="${phenotypeSummaryCriteria.fishExperiments[0].experiment}"/>
            </td>
        </tr>
    </c:if>
    <c:if test="${!empty phenotypeSummaryCriteria.searchCriteriaPhenotype}">
        <tr>
            <th>Matching Terms:</th>
            <td>
                <c:forEach var="term" items="${phenotypeSummaryCriteria.searchCriteriaPhenotype}" varStatus="index">
                    ${term.name}<c:if test="${!index.last}">,</c:if>
                </c:forEach>
            </td>
        </tr>
    </c:if>
</table>
<p/>

<p/>

<div class="summary">
    <span class="summaryTitle">Phenotypes</span>
    <zfin2:figurePhenotypeSummary figureSummaryDisplayList="${figureSummaryDisplay}"/>
</div>
