<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<div style="width: 30em; margin: 3em auto; padding-top: 1em;">

    <zfin:databaseLock locked="true">
        <span style="color:red">
            The Database us currently locked for administrative operations and will
        not allow logins. <br/>
        Please try again later or contact <a href="mailto:judys@cs.uoregon.edu">judys@cs.uoregon.edu</a>
            </span>

        <p/>
    </zfin:databaseLock>

    <zfin:databaseLock locked="false">
        <% if (request.getParameter("error") != null) { %>
    <span style="color:red">
        Wrong Login/Password. Please try again or contact <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a>
        </span>
        <% } %><br/>
        <zfin2:spiffyCorners>
            <div style="padding: 2em;">
                <form id="login" name="login" action="/action/security-check"
                      method="POST" accept-charset="UTF-8">
                    <input type="hidden" name="page" value="Main"/>
                    <label for="j_username">Login:</label>
                    <input type="text" size="12" name="j_username" id="j_username">
                    &nbsp;&nbsp;
                    <label for="j_password">Password:</label>
                    <input type="password" size="12" name="j_password" id="j_password">
                    &nbsp;&nbsp; <input type="submit" name="action" value="login"/>
                </form>
            </div>
        </zfin2:spiffyCorners>

        <script>document.login.j_username.focus();</script>
    </zfin:databaseLock>
</div>

