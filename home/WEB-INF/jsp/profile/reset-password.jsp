<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script>
    function passwordsMatch() {
        if ($('#pass1').value === undefined
            || $('#pass2').value === undefined
            || $('#pass1').value !== $('#pass2').value) {
            $('#submit').disabled = true;
        } else {
            $('#submit').removeAttr('disabled');
        }

    }
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

                    <div>
                        <label path="pass1">Password:</label>
                        <input type="password" size="50" name="pass1" id="pass1" onChange="passwordsMatch()"/>
                    </div>

                    <div>
                        <label path="pass2">Repeat Password:</label>
                        <input type="password" size="50" name="pass2" id="pass2" onChange="passwordsMatch()"/>
                    </div>

                    <br><br>

                    <input type="submit" id="submit" class="btn btn-zfin btn-block" value="Save" disabled/>

                        <%--</div>--%>
                </form>

            </div>


        </c:when>
        <c:when test="${resetSuccessful}">
            <div class="login-container">
                <div class="alert alert-primary">
                    Password successfully reset, <a href="/action/login">login here</a>
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







