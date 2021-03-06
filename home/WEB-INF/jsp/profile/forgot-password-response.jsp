<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">
    <div class="login-container">
        <c:choose>
            <c:when test="${!empty errorMessage}">
                <div class="alert alert-error">${errorMessage}</div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-info">
                    Password reset email sent
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</z:page>

