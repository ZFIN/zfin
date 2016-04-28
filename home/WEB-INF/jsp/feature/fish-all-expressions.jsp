<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="titlebar">
    <h1>Expression Summary</h1>
    <span class="yourinputwelcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Expression Summary from Fish search"/>
        </tiles:insertTemplate>
    </span>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>Fish:</th>
        <td>
            <zfin:link entity="${fish}"/>
        </td>
    </tr>
    <c:if test="${!empty fish.strList}">
        <tr>
            <th>Knockdown Reagents:</th>
            <td>
                <c:forEach var="sequenceTargetingReagent" items="${fish.strList}" varStatus="index">
                    <zfin:link entity="${sequenceTargetingReagent}"/><c:if test="${!index.last}">, </c:if>

                </c:forEach>
            </td>
        </tr>
    </c:if>
</table>
<p/>

<div class="summary">
    <div class="summaryTitle">
        All ${fn:length(geneCentricExpressionDataList)} expressed genes for:
        <zfin:link entity="${fish}"/>
    </div>

    <zfin2:all-expression expressionSummaryDisplay="${geneCentricExpressionDataList}"
                          queryKeyValuePair="fishID=${fish.fishID}"
                          suppressMoDetails="true"/>
</div>
