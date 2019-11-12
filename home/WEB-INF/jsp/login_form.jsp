<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<div class="login-container">
    <h1>Sign in to ZFIN</h1>

    <zfin:databaseLock locked="true">
    <div class="alert alert-danger" role="alert">
        <p>The Database is currently locked for administrative operations and will not allow logins.</p>
        <p>Please try again later or contact <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a></p>
    </div>
    </zfin:databaseLock>

    <% if (request.getParameter("error") != null) { %>
    <div class="alert alert-danger" role="alert">
        Incorrect username or password. Please try again or contact <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a>.
    </div>
    <% } %>

    <form id="login" name="login" action="/action/j_security-check" method="POST" accept-charset="UTF-8">
        <input type='hidden' name='_spring_security_remember_me' value="true"/>
        <input type="hidden" name="page" value="Main"/>
        <div class="form-group">
            <label for="username">Username</label>
            <input type="text" class="form-control" name="username" id="username">
        </div>

        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" class="form-control" name="password" id="password">
        </div>

        <button type="submit" class="btn btn-zfin btn-block">Sign In</button>
    </form>

    <div class="card">
        <div class="card-body">
            <p><a href="/action/profile/forgot-password">Forgot password?</a></p>
            <div>Need an account? <zfin2:mailTo>Contact us.</zfin2:mailTo></div>
        </div>
    </div>
</div>

