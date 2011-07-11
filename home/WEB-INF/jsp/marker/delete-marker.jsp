<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:choose>
    <c:when test="${!empty formBean.errors}">
        Failed to delete <zfin:link entity="${formBean.markerToDelete}"/>
        <ul>
            <c:forEach var="error" items="${formBean.errors}">
                <li><span class="error">${error}</span></li>
            </c:forEach>
        </ul>
        <a href="/action/marker/marker-edit?zdbID=${formBean.zdbIDToDelete}">[Edit]</a>

    </c:when>
    <c:otherwise>
        <b>${formBean.markerToDeleteViewString}</b> has been deleted!
    </c:otherwise>
</c:choose>
