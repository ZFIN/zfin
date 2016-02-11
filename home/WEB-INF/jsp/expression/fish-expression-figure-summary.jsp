<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td width="100%" class="titlebar">
            <span style="font-size: larger; font-weight: bold;">
            Expression Summary
                </span>

        <span style="float: right">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Expression summary for genotype"/>
            </tiles:insertTemplate>
        </td>
    </tr>
</table>

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