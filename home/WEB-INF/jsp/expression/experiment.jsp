<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <zfin2:dataManager zdbID="${experiment.zdbID}"/>

    <div><span class="name-label">Experiment Conditions Description:&nbsp;</span><span style="font-size: large;">${experiment.displayAllConditions}</span></div>

    <div class="data-page">
        <c:forEach var="condition" items="${experiment.experimentConditions}" varStatus="loop">
            <table class="primary-entity-attributes">
                <tr>
                    <th>${condition.displayName}</th>
                </tr>
            </table>

            <div class="summary">
                <c:forEach var="term" items="${condition.allTerms}">
                    <zfin2:termMiniSummary term="${term}" additionalCssClasses="summary horizontal-solidblock"/>
                </c:forEach>
            </div>
        </c:forEach>
    </div>

    <table class="primary-entity-attributes">
        <tr>
            <th>Publication:</th>
            <td><zfin:link entity="${experiment.publication}"/> </td>
        </tr>
    </table>
</z:page>