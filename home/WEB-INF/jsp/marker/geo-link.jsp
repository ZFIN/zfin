<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:choose>
    <c:when test="${!empty geolink}">
        ${geolink}
    </c:when>
    <c:otherwise>
        <span class="no-data-tag" style="background-color: inherit;">No data available</span>
    </c:otherwise>
</c:choose>
