<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script>
    $(document).ready(function() {
        $('.password-input').on('input', function() {
            var pass1 = $('#pass1');
            var pass2 = $('#pass2');
            var submit = $('#submit');
            var invalid = pass1.val() === undefined
                || pass2.val() === undefined
                || pass1.val() !== pass2.val();
            submit.prop('disabled', invalid);
        });
    });
</script>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

    <c:choose>
        <c:when test="${allowReset}">


            <c:set var='secure' value="<%=ZfinPropertiesEnum.SECURE_HTTP.toString()%>"/>
            <c:set var='domain' value="<%=ZfinPropertiesEnum.DOMAIN_NAME.toString()%>"/>
            <c:set var='secureServer' value="${secure}${domain}"/>

            <div class="login-container">

                <h1>Reset Password</h1>

                <form method="post"
                      action="${secureServer}/action/profile/password-reset/${zdbId}">
                    <input type="hidden" name="resetKey" value="${resetKey}"/>
                    <input type="hidden" name="zdbId" value="${zdbId}"/>

                    <c:if test="${errorMessage}">
                        <div class="alert alert-error">${errorMessage}</div>
                    </c:if>

                    <div class="form-group">
                        <label path="pass1">New Password</label>
                        <input type="password" size="50" class="password-input form-control" name="pass1" id="pass1"/>
                    </div>

                    <div class="form-group">
                        <label path="pass2">Confirm New Password</label>
                        <input type="password" size="50" class="password-input form-control" name="pass2" id="pass2"/>
                    </div>

                    <input type="submit" id="submit" class="btn btn-zfin btn-block" value="Save" disabled />
                </form>

            </div>


        </c:when>
        <c:when test="${resetSuccessful}">
            <div class="login-container">
                <div class="alert alert-primary">
                    Password successfully reset. <a href="/action/login">Login here.</a>
                </div>
            </div>
        </c:when>

        <c:otherwise>
            <div class="login-container">
                <div class="alert alert-danger">
                    <c:choose>
                        <c:when test="${!empty errorMessage}">
                            ${errorMessage}
                        </c:when>
                        <c:otherwise>
                            Sorry, something went wrong
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:otherwise>
    </c:choose>







