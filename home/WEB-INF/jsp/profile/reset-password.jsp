<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="${zfn:getAssetPath("profiles.js")}"></script>

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
                        <%-- need to embed the resetKey .... can't use person as formbean --%>
                    <input type="hidden" name="resetKey" value="${resetKey}"/>
                    <input type="hidden" name="zdbId" value="${zdbId}"/>

                    <c:if test="${message}">
                        <div class="alert alert-secondary">${message}</div>
                    </c:if>

                    <div>
                        <label path="pass1">Password:</label>
                        <input type="password" size="50" name="pass1" cssClass="fill-with-generated-password"
                               onkeyup="testPassword(document.getElementById('accountInfo.pass1').value,'passwordScore','passwordVerdict');"/>
                    </div>

                    <div>
                        <label path="pass2">Repeat Password:</label>
                        <input type="password" size="50" name="pass2" cssClass="fill-with-generated-password"/>
                    </div>

                    <input type="button" id="generate-password-button" value="generate password"/>
                    <span class="fill-with-generated-password"></span>

                    <div>Password Strength: <strong><span id="passwordVerdict"></span></strong></div>

                    <br><br>

                    <input type="submit" value="Save"/>

                        <%--</div>--%>
                </form>

            </div>


        </c:when>
        <c:when test="${resetSuccessful}">
            <div class="alert alert-primary">
                Password successfully reset, <a href="/action/login">login here</a>
            </div>
        </c:when>

        <c:otherwise>
            <div class="alert alert-danger">
                Sorry, something went wrong
            </div>
        </c:otherwise>
    </c:choose>







