<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="links" type="java.util.Collection"%>

<c:if test="${!empty links}">
    <div class="btn-group">
        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            Related <span class="caret"></span>
        </button>
        <ul class="dropdown-menu pull-right">
            <c:forEach var="link" items="${links}">
                <li>${link}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>
