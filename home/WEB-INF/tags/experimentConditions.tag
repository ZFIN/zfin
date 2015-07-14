<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="conditions" type="java.util.Collection" required="true" %>

<c:if test="${!empty conditions}">
    <div class="summary">
        <div class="summaryTitle">Conditions</div>
        <table class="summary rowstripes">
            <tr>
                <th>Category</th>
                <th>Name</th>
                <th>Comments</th>
            </tr>
            <c:forEach var="condition" items="${conditions}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td>
                        ${condition.conditionDataType.group}
                    </td>
                    <td>
                        ${condition.conditionDataType.name}
                    </td>
                    <td>
                        ${condition.comments}
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </div>
</c:if>