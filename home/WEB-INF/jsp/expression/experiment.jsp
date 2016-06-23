<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:dataManager zdbID="${experiment.zdbID}"/>

<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${experiment.zdbID}"/>
    </tiles:insertTemplate>
</div>

<div class="data-page">
    <c:forEach var="condition" items="${conditions}" varStatus="loop">
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