<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">
    <div class="login-container">
        <c:choose>
            <c:when test="${!empty username}">

                <h1>Expired Password</h1>

                <p>
                    Your password has expired. Please click next to begin the process to reset your password.
                    You will receive an email with a link to reset your password.
                </p>

                <form action="/action/profile/forgot-password" class="login-box" method="post">
                    <div class="form-group">
                        <input type="hidden" class="form-control" name="emailOrLogin" value="${username}"
                               id="emailOrLogin">
                    </div>

                    <button type="submit" class="btn btn-primary btn-block">Next</button>
                </form>
            </c:when>
            <c:otherwise>
                <p class="alert alert-danger">Error, You must be logged in to access this page. Redirecting now.</p>
                <meta http-equiv="refresh" content="2;url=/action/login">
            </c:otherwise>
        </c:choose>
    </div>
</z:page>