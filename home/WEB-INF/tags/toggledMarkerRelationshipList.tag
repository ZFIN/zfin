<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="collection" required="true" rtexprvalue="true" type="java.util.List" %>
<%@ attribute name="maxNumber" required="false" type="java.lang.Integer" rtexprvalue="true" %>

<c:if test="${fn:length(collection) > 0 }">
    <ul class="comma-separated" data-toggle="collapse" data-show="${maxNumber}">
        <c:forEach var="entry" items="${collection}">
            <li>${entry.link} ${entry.attributionLink}</li>
        </c:forEach>
    </ul>
</c:if>


