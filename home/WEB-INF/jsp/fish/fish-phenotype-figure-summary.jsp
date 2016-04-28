<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="phenotypeSummaryCriteria" class="org.zfin.fish.presentation.PhenotypeSummaryCriteria" scope="request"/>

<div class="titlebar">
    <h1>Phenotype Summary</h1>
    <span class="yourinputwelcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Phenotype summary from Fish search"/>
        </tiles:insertTemplate>
    </span>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>Fish:</th>
        <td>
            <zfin:link entity="${fish}"/>
        </td>
    </tr>
    <tr>
        <th>Conditions:</th>
        <td>
            All
        </td>
    </tr>
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
