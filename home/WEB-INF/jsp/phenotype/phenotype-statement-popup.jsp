<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    Phenotype: <zfin:name entity="${phenotypeStatement}"/>
</div>

<div class="popup-body phenotype-popup-body">
    <div>
        <c:forEach var="term" items="${uniqueTerms}" varStatus="loop">
            <c:if test="${!loop.first}">
                <hr class="popup-divider"/>
            </c:if>
            <zfin2:termMiniSummary term="${term}"/>
        </c:forEach>

        <c:if test="${!empty tagNote}">
            <hr class="popup-divider"/>
            <div class="ontology-term-mini-summary">
                <table class="ontology-term-mini-summary">
                    <tr>
                        <th class="name">Tag:</th>
                        <td class="name">${phenotypeStatement.tag}</td>
                    </tr>
                    <tr>
                        <td>Definition:</td>
                        <td>${tagNote}</td>
                    </tr>
                </table>
            </div>
        </c:if>
    </div>
</div>


