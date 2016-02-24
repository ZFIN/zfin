<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    Phenotype: <zfin:name entity="${phenotypeStatement}"/>
</div>

<div class="popup-body phenotype-popup-body">
    <div>

        <c:if test="${phenotypeStatement.tag eq 'normal'}">
            <div style="margin: 1em;">
                The "normal or recovered" tag is used when the annotation of a normal phenotype is notable
                or when the annotation represents a recovered normal phenotype, such as that
                resulting from the addition of a sequence targeting reagent or the creation of a complex
                mutant genotype.
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


