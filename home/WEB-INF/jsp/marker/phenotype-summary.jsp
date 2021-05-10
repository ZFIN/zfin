<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <div class="data-sub-page-title">Phenotype Figure Summary</div>


    <table class="primary-entity-attributes">
        <c:if test="${!empty marker}">
            <tr>
                <th class="genotype-name-label">Marker:</th>
                <td class="genotype-name-value"><zfin:link entity="${marker}"/></td>
            </tr>
            <tr>
                <th class="genotype-name-label">Conditions:</th>
                <td class="genotype-name-value">Standard or Control <a class='popup-link info-popup-link' href='/action/marker/note/phenotype-summary-note'></a></td>
            </tr>
        </c:if>
    </table>

    <div class="summary">
        <zfin2:figureSummary figureSummaryList="${figureSummaryDisplayList}" showMarker="false"
                             expressionData="false" phenotypeData="true" showGenotype="true"/>
    </div>
</z:page>