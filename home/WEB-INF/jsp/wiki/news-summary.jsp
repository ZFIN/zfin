<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${!empty summaries}">
    <ul>
        <c:forEach var="summary" items="${summaries}" varStatus="loop" >
            <c:if test="${length <0 || loop.index < length}">
                <li>
                    <a href="${summary.url}">${summary.title}</a>
                     <fmt:formatDate value="${summary.publishDate.time}"/>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</c:if>
