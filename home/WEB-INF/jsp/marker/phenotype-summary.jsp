<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${marker.name}"/>
    </tiles:insertTemplate>
</div>
<div class="data-sub-page-title">Phenotype Figure Summary</div>


<table class="primary-entity-attributes">
    <c:if test="${!empty marker}">
        <tr>
            <th class="genotype-name-label">Marker:</th>
            <td class="genotype-name-value"><zfin:link entity="${marker}"/></td>
        </tr>
        <tr>
            <th class="genotype-name-label">Conditions:</th>
            <td class="genotype-name-value">Standard or Control <small><a class='popup-link info-popup-link' href='/action/marker/note/phenotype-summary-note'></a></small> </td>
        </tr>
    </c:if>
</table>

<div class="summary">
    <zfin2:figureSummary figureSummaryList="${figureSummaryDisplayList}" showMarker="false"
                         expressionData="false" phenotypeData="true" showGenotype="true"/>
</div>