<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>


<div style="width: 30em; margin: 3em auto; padding-top: 1em;">

    <zfin:databaseLock locked="true">
        <span style="color:red">
            The Database is currently locked for administrative operations and will
        not allow logins. <br/><br/>
        Please try again later or contact <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a>
            </span>

        <p/>
    </zfin:databaseLock>

    <% if (request.getParameter("error") != null) { %>
    <span style="color:red">
        Wrong Login/Password. Please try again or contact <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a>
        </span>
    <% } %><br/>

    <div class="login-box">
        <form id="login" name="login" action="/action/j_security-check"
              method="POST" accept-charset="UTF-8">
            <input type='hidden' name='_spring_security_remember_me' value="true"/>
            <input type="hidden" name="page" value="Main"/>
            <label for="username">Login:</label>
            <input type="text" size="12" name="username" id="username">
            &nbsp;&nbsp;
            <label for="password">Password:</label>
            <input type="password" size="12" name="password" id="password">
            &nbsp;&nbsp; <input type="submit" name="action" value="login"/>
        </form>
    </div>

    <script>
        $(function () {
            document.login.username.focus();
            var elements = document.getElementsByTagName("a");
            for (var i = 0; i < elements.length; i++) {
                var link = elements[i];
                if (link.href != null) {
                    link.href = link.href.replace("https", "http");
                }
            }
        });
    </script>
</div>

