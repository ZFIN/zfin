<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">
    <div class="login-container border mt-5 p-4 rounded">
        <c:choose>
            <c:when test="${!empty errorMessage}">
                <div class="alert alert-error">${errorMessage}</div>
            </c:when>
            <c:otherwise>
                <h3 class="center">Email Sent</h3>
                <div class="alert alert-info center">
                    Password reset email sent
                </div>
                <p>
                    Please check your email inbox for a message from ZFIN.  Follow the link to set your new password.
                </p>
            </c:otherwise>
        </c:choose>
    </div>
</z:page>

