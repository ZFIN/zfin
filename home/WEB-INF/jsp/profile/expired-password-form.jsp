<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">
    <div class="login-container border mt-5 p-4 rounded">
        <c:choose>
            <c:when test="${!empty username}">
                <h3 class="center">Expired Password</h3>
                <div class="alert alert-danger center">
                    Your password has expired
                </div>
                <p>
                    Please click next to begin the password reset process.
                    You will receive an email that contains a link to reset your password.
                </p>

                <form action="/action/profile/forgot-password" class="login-box" method="post">
                    <div class="form-group">
                        <input type="hidden" class="form-control" name="emailOrLogin" value="${username}" id="emailOrLogin">
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