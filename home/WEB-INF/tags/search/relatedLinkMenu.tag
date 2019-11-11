<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="links" type="java.util.Collection"%>

<c:if test="${!empty links}">
    <div class="btn-group">
        <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            Related
        </button>
        <div class="dropdown-menu pull-right">
            <c:forEach var="link" items="${links}">
                ${link}
            </c:forEach>
        </div>
    </div>
</c:if>
