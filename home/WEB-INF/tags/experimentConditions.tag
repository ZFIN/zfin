<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="morpholinoConditions" type="java.util.Collection" %>
<%@ attribute name="nonMorpholinoConditions" type="java.util.Collection" %>


<c:if test="${!empty morpholinoConditions}">
<div class="summary">
    <div class="summaryTitle">Morpholino</div>
    <table class="summary rowstripes">
        <tr>
            <th>Morpholino</th>
            <th>Value</th>
            <th>Unit</th>
            <th>Comments</th>
        </tr>
        <c:forEach var="condition" items="${morpholinoConditions}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${condition.morpholino}"/>
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


<c:if test="${!empty nonMorpholinoConditions}">
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
        <c:forEach var="condition" items="${nonMorpholinoConditions}" varStatus="loop">
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