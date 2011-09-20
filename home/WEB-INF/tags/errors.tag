<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="errorResult" type="org.springframework.validation.BindingResult" required="false" rtexprvalue="true" %>
<%@ attribute name="cssClass" type="java.lang.String" required="false" %>
<%@ attribute name="path" required="false" rtexprvalue="true" %>

<c:if test="${empty cssClass}">
    <c:set var="cssClass" value="error-inline"/>
</c:if>

<c:if test="${empty errorResult}">
    <c:set var="errorResult" value="${errors}"/>
</c:if>

<c:if test="${errorResult.errorCount >0}">
    <c:choose>
        <c:when test="${empty path}">
            <div class="${cssClass}">
                <ul>
                    <c:forEach var="error" items="${errorResult.allErrors}">
                        <li>${error.defaultMessage}</li>
                    </c:forEach>
                </ul>
            </div>
        </c:when>
        <c:otherwise>
            <c:forEach var="error" items="${errorResult.allErrors}">
                <c:catch var="exception">${error.field}</c:catch>
                <c:if test="${exception==null && error.field eq path}">
                    <div class="${cssClass}"> ${error.defaultMessage}</div>
                </c:if>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</c:if>

