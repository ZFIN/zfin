<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="date" type="java.util.Date" %>

<c:choose>
    <c:when test="${empty date}">
        &mdash;
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${zfn:isToday(date)}">
                Today
            </c:when>
            <c:when test="${zfn:isTomorrow(date)}">
                Tomorrow
            </c:when>
            <c:when test="${zfn:isYesterday(date)}">
                Yesterday
            </c:when>
            <c:otherwise>
                <fmt:formatDate value="${date}" type="both" pattern="d MMM"/>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
