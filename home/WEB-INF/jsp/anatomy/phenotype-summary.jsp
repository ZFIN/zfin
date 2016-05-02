<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${fish.name}"/>
    </tiles:insertTemplate>
</div>
<div class="data-sub-page-title">Phenotype Figure Summary </div>


<table class="primary-entity-attributes">
    <c:if test="${!empty fish}">
        <tr>
            <th class="genotype-name-label">Fish:</th>
            <td class="genotype-name-value"><zfin:link entity="${fish}"/></td>
        </tr>
    </c:if>
    <c:if test="${!empty entity}">
        <tr>
            <th>Term:</th>
            <td><zfin:link entity="${entity}" suppressPopupLink="false"/>
        <c:if test="${!empty includingSubstructures}"> or substructures
        </c:if>

        </td>
        </tr>
    </c:if>

    <c:if test="${standardEnvironment}">
        <tr>
            <th>Condition:</th>
            <td>Standard or generic control only</td>
        </tr>
    </c:if>
    <c:if test="${chemicalEnvironment}">
        <tr>
            <th>Condition:</th>
            <td>Chemical environments only</td>
        </tr>
    </c:if>
    <c:if test="${wildtypeOnly}">
        <tr>
            <th>Background:</th>
            <td>Wild-type only</td>
        </tr>
    </c:if>


</table>

<div class="summary">
    <zfin2:figureSummary figureSummaryList="${figureSummaryDisplayList}" showMarker="false" expressionData="false" phenotypeData="true"/>
</div>