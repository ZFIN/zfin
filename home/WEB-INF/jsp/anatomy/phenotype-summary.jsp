<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="data-sub-page-title">Phenotype Figure Summary</div>


<table class="primary-entity-attributes">
    <c:if test="${!empty genotype}">
        <tr>
            <th class="genotype-name-label">Genotype:</th>
            <td class="genotype-name-value"><zfin:link entity="${genotype}"/></td>
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
    <zfin2:figureSummary figureExpressionSummaryList="${figureSummaryDisplayList}" showMarker="false" expressionData="false" phenotypeData="true"/>
</div>