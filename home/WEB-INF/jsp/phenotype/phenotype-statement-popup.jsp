<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    Phenotype: <zfin:name entity="${phenotypeStatement}"/>
</div>

<div class="popup-body phenotype-popup-body">
    <div>

        <c:if test="${!empty tagNote}">
            <div style="margin: 1em;">
                ${tagNote}
            </div>
            <hr class="popup-divider"/>
        </c:if>

        <c:forEach var="term" items="${uniqueTerms}" varStatus="loop">
            <c:if test="${!loop.first}">
                <hr class="popup-divider"/>
            </c:if>
            <zfin2:termMiniSummary term="${term}"/>
        </c:forEach>


    </div>
</div>


