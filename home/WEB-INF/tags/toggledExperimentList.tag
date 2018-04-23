<%@ tag body-content="scriptless" %>
<%@attribute name="experimentList" type="java.util.Collection" %>
<%@attribute name="maxNumber" type="java.lang.Integer" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${fn:length(experimentList) > 0 }">
    <ul class="comma-separated" data-toggle="collapse" data-show="${maxNumber}">
        <c:forEach var="experiment" items="${experimentList}">
            <li><zfin:experiment experiment="${experiment}"/></li>
        </c:forEach>
    </ul>
</c:if>
