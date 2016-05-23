<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<div class="titlebar">
    <h1>Expression Summary</h1>
    <span class="yourinputwelcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Expression summary for genotype"/>
        </tiles:insertTemplate>
    </span>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>Fish:</th>
        <td>
            <zfin:link entity="${expressionCriteria.fish}"/>
        </td>
    </tr>
    <tr>
        <th>Conditions:</th>
        <td>
            All
        </td>
    </tr>
</table>

<div class="summary">
    <span class="summaryTitle">Expressions</span>
    <zfin2:figureSummary figureSummaryList="${figureSummaryDisplayList}" expressionData="true"
                         showMarker="true"/>
</div>