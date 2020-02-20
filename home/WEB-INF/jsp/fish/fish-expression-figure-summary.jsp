<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="phenotypeSummaryCriteria" class="org.zfin.fish.presentation.PhenotypeSummaryCriteria" scope="request"/>

<div class="titlebar">
    <h1>Expression Summary</h1>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>Fish:</th>
        <td>
            <zfin:link entity="${phenotypeSummaryCriteria.fishExperiments[0].fish}"/>
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
    <span class="summaryTitle">Expressions</span>
    (<zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                  integerEntity="${figureCount}"/>)
    <%--
        <span style="float: right">
            <c:if test="${fn:length(phenotypeSummaryCriteria.searchCriteriaPhenotype) > 0}">
                [ <a
                    href="javascript:document.location.replace('?fishID=${phenotypeSummaryCriteria.fish.fishID}')">
                Remove matching terms</a> ]
            </c:if>
        </span>
    --%>
    <zfin2:figureSummary figureSummaryList="${figureExpressionSummaryDisplayList}" expressionData="true" showMarker="true"/>
</div>
