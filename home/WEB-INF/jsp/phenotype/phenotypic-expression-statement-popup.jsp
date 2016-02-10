<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    Phenotype: <zfin:name entity="${phenotypeStatement}"/>
</div>

<div class="popup-body phenotype-popup-body">
    <div>

        <zfin2:geneSummary gene="${phenotypeStatement.gene}" previousNames="${genePreviousNames}"/>
        <hr class="popup-divider"/>
        <zfin2:termMiniSummary term="${phenotypeStatement.e1a}"/>
        <hr class="popup-divider"/>
        <c:if test="${not empty phenotypeStatement.e1b}">
            <zfin2:termMiniSummary term="${phenotypeStatement.e1b}"/>
            <hr class="popup-divider"/>
        </c:if>
        <zfin2:termMiniSummary term="${phenotypeStatement.quality}"/>

    </div>
</div>
