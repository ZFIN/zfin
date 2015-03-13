<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequenceTargetingReagentConditions" type="java.util.Collection" %>
<%@ attribute name="nonSequenceTargetingReagentConditions" type="java.util.Collection" %>


<c:if test="${!empty sequenceTargetingReagentConditions}">
    <div class="summary">
        <div class="summaryTitle">Knockdown Reagents</div>
        <table class="summary rowstripes">
            <tr>
                <th>Reagent</th>
                <th>Value</th>
                <th>Unit</th>
                <th>Comments</th>
            </tr>
            <c:forEach var="condition" items="${sequenceTargetingReagentConditions}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td>
                        <zfin:link entity="${condition.sequenceTargetingReagent}"/>
                    </td>

                    <td>
                        ${condition.value}
                    </td>
                    <td>
                        ${condition.unit.name}
                    </td>
                    <td>
                        ${condition.comments}
                    </td>

                </zfin:alternating-tr>
            </c:forEach>
        </table>

    </div>
</c:if>


<c:if test="${!empty nonSequenceTargetingReagentConditions}">
    <div class="summary">
        <div class="summaryTitle">Conditions</div>
        <table class="summary rowstripes">
            <tr>
                <th>Category</th>
                <th>Name</th>
                <th>Value</th>
                <th>Unit</th>
                <th>Comments</th>
            </tr>
            <c:forEach var="condition" items="${nonSequenceTargetingReagentConditions}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td>
                        ${condition.conditionDataType.group}
                    </td>
                    <td>
                        ${condition.conditionDataType.name}
                    </td>
                    <td>
                        ${condition.value}
                    </td>
                    <td>
                        ${condition.unit.name}
                    </td>
                    <td>
                        ${condition.comments}
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </div>
</c:if>