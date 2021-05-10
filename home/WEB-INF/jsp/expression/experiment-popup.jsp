<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    Environment Description: <zfin:name entity="${experiment}"/>
</div>
<div class="popup-body">
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