
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<div class="titlebar">
    <h1>Expression Summary</h1>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>Genotype:</th>
        <td>
            <zfin:link entity="${expressionCriteria.genotype}"/>
        </td>
    </tr>
</table>


<div class="summary">
    <span class="summaryTitle">Expressions</span>
    (<zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                  integerEntity="${figureCount}"/>)

    <zfin2:figureSummary figureSummaryList="${figureSummaryDisplayList}" expressionData="true" showMarker="true"/>
</div>