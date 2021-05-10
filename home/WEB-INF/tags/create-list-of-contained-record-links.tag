<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="value" type="java.lang.String" required="true" %>
<%@ attribute name="identifier" type="java.lang.String" required="true" %>

<c:choose>
    <c:when test="${fn:contains(value, 'ZDB-')}">
        <c:if test="${fn:contains(value, ',')}">
            <c:forEach var="singleSingleValue" items="${fn:split(value, ',')}">
                <c:if test="${fn:startsWith(singleSingleValue, 'ZDB-')}">
                    <a href="/action/database/view-record/${singleSingleValue}">
                        <span class="auto-name-fetchable">${singleSingleValue}</span></a>,
                </c:if>
            </c:forEach>
        </c:if>
        <c:if test="${fn:contains(value, '|')}">
            <c:forEach var="singleValue" items="${fn:split(value, '|')}">
                <c:if test="${fn:startsWith(singleValue, 'ZDB-')}">
                    <a href="/action/database/view-record/${singleValue}">${singleValue}</a>,
                </c:if>
            </c:forEach>
        </c:if>
        <c:if test="${!fn:contains(value, ',')}">
            <c:if test="${!fn:contains(value, '|')}">
                <a href="/action/database/view-record/${value}">
                    <span class="fetchable">${value}</span></a>
                </a>
            </c:if>
        </c:if>
    </c:when>
    <c:otherwise>
        ${value}
    </c:otherwise>
</c:choose>
