<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.people.presentation.ProfileBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<SCRIPT type="text/javascript">

    function check_login(src) {
        document.login_form.user.login.value = vet_pw(document.login_form.login.value);
        if (document.login_form.login.value == '') {
            document.login_form.login.focus();
            src = 0;
        }
        else {
            if (document.login_form.password.value == '') {
                document.login_form.password.focus();
            } else {
                if (document.login_form.password2.value == '') {
                    document.login_form.password2.focus();
                }
                else {
                    submit_complete(src + 1);
                }
            }
        }
    }

    function check_passw(src) {
        document.login_form.password.value = vet_pw(document.login_form.password.value);

        if (document.login_form.password.value != '') {
            if (document.login_form.password2.value == '') {
                document.login_form.password2.focus();
                return;
            } else if (document.login_form.password.value != document.login_form.password2.value) {
                window.alert("both passwords must be the same");
                document.login_form.password.value = '';
                document.login_form.password.focus();
                src = 0;
                return;
            }
            else {//document.login_form.commit.focus();return;
                submit_complete(src + 2);
            }
        }
        else {
            document.login_form.password.focus();
            src = 0;
            return;
        }
    }

    function check_passw2(src) {
        document.login_form.password2.value = vet_pw(document.login_form.password2.value);
        if (document.login_form.password2.value != '') {
            if (document.login_form.password.value == '') {
                document.login_form.password.focus();
                return;
            } else
                if (document.login_form.password2.value != document.login_form.password.value) {
                    window.alert("both passwords must be the same");
                    document.login_form.password2.value = '';
                    document.login_form.password2.focus();
                    src = 0;
                    return;
                }
        }
        else {
            document.login_form.password2.focus();
            src = 0;
            return;
        }
    }

    function submit_complete(src) {
        if (! src & 1) {
            check_login(src);
        }
        if (! src & 2) {
            check_passw(src);
        }
        if (! src & 4) {
            check_passw2(src);
        }
        formLogin = document.getElementById('login');
        formPassword = document.getElementById('password');
        formPassword2 = document.getElementById('password2');
        if ((formLogin.value != '') &&
            (formPassword.value != '') &&
            (formPassword.value == formPassword2.value)
                ) {
            document.login_form.action.value = '<%=ProfileBean.ACTION_EDIT%>';
            document.login_form.submit();
        }
        else {
            src = 0;
            window.alert("SORRY! Your login and or password is not acceptable.\n Please try again.");
        }
    }

    function submit_delete() {
        document.login_form.action.value = '<%=ProfileBean.ACTION_DELETE%>';
        document.login_form.submit();
    }

    function cancel() {
        window.location.replace("/<%=ZfinProperties.getWebDriver()%>?MIval=aa-persview.apg&OID=${formBean.person.zdbID}");
    }

    function vet_pw(str) {
        var optout = /[^\-\.0123456789ABCDEFGHIJKLMNOPRSTUVWZ\_abcdefghijklmnopqrstuvwxyz]/;
        //window.alert(str);
        if (optout.test(str)) {
            window.alert("Acceptable chars are:\n\tA-Za-z\n\t0123456789\n\t-._");
            str = '';
        }
        //window.alert(str);
        return str;
    }
</SCRIPT>

<div class="allcontent">

    <c:choose>
        <c:when test="${formBean.ownerOrRoot}">

            <form:form action="/action/people/edit-user?person.zdbID=${formBean.person.zdbID}" commandName="formBean"
                       method="POST"
                       name="login_form">

                <c:choose>
                    <c:when test="${formBean.newUser}">
                        <center><big><Font color="#ff0000">
                            This person is not currently a registered ZFIN user.
                            Creating a new registered user...
                        </font></big></center>
                        <h1 align=center> CREATE NEW ZFIN USER</h1>
                        <form:hidden path="newUser"/>
                    </c:when>
                    <c:otherwise>
                        <h1 align=center> Edit registered ZFIN user</h1>
                    </c:otherwise>
                </c:choose>

                <b>Make desired changes to the ZFIN registered user, then click on COMMIT
                    button. Or you may click "CANCEL" to abort the update.</b>

                <p>

                <center>
                    <TABLE border=1 cellpadding=8>
                        <TR align=center>
                            <TD>

                                <TABLE>
                                    <TR align=center>
                                        <TD>
                                            <b>FULL NAME:</b><br> <font size=-1>
                                            (Format: Joe J. Smith)
                                        </font>
                                        </TD>
                                        <TD><font size=+2>

                                            <form:input path="accountInfo.name"/>
                                        </font>
                                            <br> (<font size=-1>${formBean.person.zdbID} </font>)
                                        </TD>
                                    </TR>
                                </TABLE>
                            </TD>
                        </TR>

                    </TABLE>
                </center>

                <authz:authorize ifAllGranted="root">
                    <TABLE width=100% cellpadding=5>
                        <TR align=center>
                            <TD>
                                <b>LOGIN:</b>
                                <form:input path="accountInfo.login" size="12" onclick="check_login(1)" maxlength="12"
                                            id="login"/>
                                <form:errors path="accountInfo.login" cssClass="Error"/>
                            </TD>
                            <TD>
                                <b>ACCESS:</B>
                                <form:select path="accountInfo.role">
                                    <form:option value="submit"/>
                                    <c:if test="${formBean.person.accountInfo.role eq 'root'}">
                                        <form:option value="root"/>
                                    </c:if>
                                </form:select>
                                <form:errors path="accountInfo.role" cssClass="Error"/>
                            </TD>
                        </TR>
                    </TABLE>
                </authz:authorize>
                <authz:authorize ifAllGranted="submit">
                    <TABLE width=100% cellpadding=5>
                        <TR align=center>
                            <TD>
                                <b>LOGIN:</b>
                                <form:hidden path="accountInfo.login" id="login"/>
                                    ${formBean.person.accountInfo.login}
                            </TD>
                            <TD>
                                <b>ACCESS:</B>
                                <input type="hidden" name="accountInfo.role" value="${formBean.accountInfo.role}"/>
                                    ${formBean.person.accountInfo.role}
                            </TD>
                        </TR>
                    </TABLE>
                </authz:authorize>
                <TABLE width=100% cellpadding=5>

                    <TR align=center>
                        <TD><b>PASSWORD:</b>
                            <form:password path="passwordOne" onchange="check_passw(2);" size="8" maxlength="8"
                                           id="password"/>
                            <form:errors path="passwordOne" cssClass="Error"/>
                        </TD>
                        <TD>
                            <b>Re-Type PASSWORD</b> <font size=-1> (to verify)</font>:
                            <form:password path="passwordTwo" onchange="check_passw2(4);" size="8" maxlength="8"
                                           id="password2"/>
                            <form:errors path="passwordTwo" cssClass="Error"/>
                        </TD>
                    </TR>
                </TABLE>

                <b>Note on Passwords:</b> <font size=-1>For security reasons,
                current password is not displayed.

                For a new user, you <b>must</b> designate a password. If editing an
                existing ZFIN user, leaving these fields blank leaves current
                password unchanged.
                Acceptable password characters are: <b>A-Za-z0-9!#-._</b> Passwords are limited to 8 characters.

                Note that characters in passwords are displayed as asterisks for security
                reasons.</font>

                <p>

                <hr width=80%>

                <input type=button value="CANCEL" onClick="cancel();">
                <input type=button value="COMMIT" name="commit" onClick="submit_complete(8);">

                <hr width=80%>

                <BIG><B>Or you may...</B></BIG>
                <TABLE cellpadding=5 width=100%>
                    <TR align=center>
                        <TD>
                            <B>
                                <input type=button name="Deleteme" value="Permanently Delete" onClick="submit_delete()">
                                <form:hidden path="action"/>
                                this user's ZFIN registration </B>
                            <font size=-1>(but not their PERSON record)</font>
                        </TD>
                    </TR>
                </TABLE>
            </form:form>
        </c:when>
        <c:otherwise>
            <div class="allcontent">


                <h1 align="center"><font color="#ff0000">NOTIFICATION: SECURITY VIOLATION </font></h1>

                <big>
                    You have attempted to access a page for which you are not authorized. This means one of two
                    things:</big>
                <ol>
                    <li><b>You have tried to do something you simply aren't authorized to do.</b> You may <a
                            href="mailto:zfinadmn@zfin.org">send mail to the ZFIN administrator</a> if you think you
                        <i>should</i> have access.

                    </li>
                    <li><b>Your authorization has expired.</b> You have not logged in since starting
                        your current browser session, or you have not accessed ZFIN for more than a
                        day, causing the system to revoke your authorization as a security
                        precaution. Please return to the <a href="/">ZFIN home page</a>
                        and log in again.
                    </li>
                </ol>

                <!-- ends cond authorization -->
            </div>
        </c:otherwise>
    </c:choose>
</div>