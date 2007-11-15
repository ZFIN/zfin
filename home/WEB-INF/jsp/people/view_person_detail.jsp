<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<SCRIPT>

    function doit(attr, attr_type) {
        window.location.replace("/@TARGETCGIBIN@/webdriver?MIval=aa-persupdate.apg&OID=ZDB-PERS-000914-2&attr=" + attr + "&attr_type=" + attr_type)
    }

    function subscribe(curr_value) {
        window.location.replace("/@TARGETCGIBIN@/webdriver?MIval=aa-update-person.apg&OID=ZDB-PERS-000914-2&attr=on_dist_list&attr_type=subscription&old_value=" + curr_value)
    }
</SCRIPT>

<SCRIPT>
    if (is_nav4) {
        document.write('<LINK rel=stylesheet type="text/css" href="/nn4_zfin_style.css">');
    } else {
        document.write('<LINK rel=stylesheet type="text/css" href="/zfin_style.css">');
    }

</SCRIPT>


<table cellpadding=5 width=100%>
<tr><td>
<TABLE WIDTH=100% border=0 bgcolor="#EEEEEE">
    <TR align=center>
        <TD><font size=-1><b>ZFIN ID:</b> <c:out value="${profileForm.person.zdbID}"/></font>
        </TD>
        <zfin:authorize role="root,adminasst" entityZdbID="${profileForm.person.zdbID}" owner="true"
                        className="org.zfin.people.Person">
        <td>
            <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=$MIval&UPDATE=1&orgOID=$orgOID&OID=$OID"><font size=-1
                                                                                                             color=red>
                Update this Record</font></A>
        </td>
        </zfin:authorize>
        <zfin:authorize entityZdbID="${profileForm.person.zdbID}" owner="true"
                        className="org.zfin.people.Person">
        <td>
            <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=$MIval&UPDATE=1&orgOID=$orgOID&OID=$OID"><font size=-1
                                                                                                             color=red>
                Change Password</font></A>
        </td>
        </zfin:authorize>
        <authz:authorize ifAllGranted="root">
        <td>
            <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-edit_user.apg&OID=$OID"><font size=-1 color=red>
                Add/Edit ZFIN Registration
            </font></A>
        </td>
        <td>
            <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-delete_record.apg&OID=$OID&rtype=$rtype"><font size=-1
                                                                                                                color=red>
                Delete this record
            </font></A>
        </td>
        </authz:authorize>
        <zfin:authorize role="root" entityZdbID="${profileForm.person.zdbID}" owner="true"
                        className="org.zfin.people.Person">
        <td>
            <A HREF="/action/audit-log/details?zdbID=<c:out value='${profileForm.person.zdbID}' />">
                Last Update:
                <c:choose>
                    <c:when test="${profileForm.latestUpdate != null}">
                        <fmt:formatDate value="${profileForm.latestUpdate.dateUpdated}" type="date"/>
                    </c:when>
                    <c:otherwise>Never modified</c:otherwise>
                </c:choose>
            </A>
        </td>
        </zfin:authorize>
</TABLE>
<TABLE width=100% cellpadding=5>
    <TR valign=middle align=center>
        <TD>
            <FONT SIZE=+2><B><c:out value="${profileForm.person.fullName}"/></B></FONT>
            <br>
            <!-- Admin info -->
            <%--
                        <zfin:authorize role="root" beanName="profileForm" primaryKey="person.zdbID" owner="true" className="org.zfin.people.Person">
            --%>
            <!-- toDo: the roles are hard coded here. Better would be a component approach ala JSF where
                 te logic would be controlled in Java class -->
            <authz:authorize ifAnyGranted="root,adminasst">
                <table border="1" cellpadding="3" width="60%" bgcolor="#eeeeee">
                    <tbody><tr><td colspan="2">
                        <b>Admin info:</b></td>
                    </tr>
                        <tr>
                            <td>Login: </td>
                            <td width="50%"><c:out value="${profileForm.user.login}"/></td>
                        </tr>
                        <tr>
                            <td>Role</td><td><c:out value="${profileForm.user.role}"/></td>
                        </tr>
                        <tr>
                            <td>Last Login: </td><td>Date: <fmt:formatDate value="${profileForm.user.loginDate}"
                                                                           type="date"/>
                            <br/> Time: <fmt:formatDate value="${profileForm.user.loginDate}" type="time"/></td>
                        </tr>
                        <tr>
                            <td>Previous Login: </td><td>Date: <fmt:formatDate
                                value="${profileForm.user.previousLoginDate}"
                                type="date"/>
                            <br/> Time: <fmt:formatDate value="${profileForm.user.previousLoginDate}" type="time"/></td>
                        </tr>
                        <tr>
                            <td>Account Created: </td><td>Date: <fmt:formatDate
                                value="${profileForm.user.accountCreationDate}"
                                type="date"/>
                            <br/> Time: <fmt:formatDate value="${profileForm.user.accountCreationDate}" type="time"/>
                        </td>
                        </tr>
                        <tr>
                            <td> Is on email distribution list</td>
                            <td><c:out value="${profileForm.person.emailList}"/>
                            </td>
                        </tr></tbody></table>
            </authz:authorize>

            <c:forEach var="lab" items="${profileForm.person.labs}">
                <a HREF="/action/people/view-lab-detail?labId=<c:out value="${lab.zdbID}"/>">
                    <c:out value="${lab.name}"/>
                </a>
                <br>
            </c:forEach>
            <c:out value="${profileForm.person.address}" escapeXml="false"/>
            <br>
        </TD>
        <TD>
            <IMG SRC="/images/LOCAL/smallogo.gif">
        </TD>
    </TR>
</TABLE>

<hr>

<TABLE width=100%>

    <TR>
        <TD align=right>

            <b>Phone: </b></TD>

        <TD><c:out value="${profileForm.person.phone}"/>
        </TD>

        <TD align=right>

            <b>Email: </b></TD>
        <TD>
            <A HREF="mailto:mhaendel@uoneuro.uoregon.edu"><c:out value="${profileForm.person.email}"/></A>
        </TD>
    </TR>

    <TR>

        <TD align=right>

            <b>FAX: </b></TD>
        <TD><c:out value="${profileForm.person.fax}"/></TD>

        <TD align=right>

            <b>URL: </b></TD>
        <TD>
            <a href="<c:out value="${profileForm.person.url}"/>">
                <c:out value="${profileForm.person.url}"/>
            </a>
        </TD>
    </TR>

</TABLE>

<HR>

<B>Biography and Research Interests:</B>

<p>
<HR>

<B>Publications:</B>


<br>


<c:forEach var="publication" items="${profileForm.person.publications}">
    <TR><TD colspan=1 align=left><DIV class="show_pubs">
        <A
                HREF="/action/publication/view-publication-detail?publication.zdbID=<c:out value='${publication.zdbID}'/>">
            <c:out value="${publication.citation}"/>
        </A></DIV></TD></TR>
</c:forEach>

<TR><TD>

    <B> Publications not related to Zebrafish:</B></TD></TR>

<p>
    <TR>
        <TD>
            <c:out value="${profileForm.person.nonZfinPublications}" escapeXml="false"/>
        </TD>
    </TR>

<p>
</td></tr></table>
