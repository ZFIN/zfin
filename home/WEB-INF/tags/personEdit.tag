<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="person" type="org.zfin.profile.Person" required="true" %>
<%@ attribute name="securityPersonZdbID" type="java.lang.String" required="true" %>
<%@ attribute name="showDeceasedCheckBox" type="java.lang.Boolean" required="true" %>
<%@ attribute name="countryList" type="java.util.HashMap" required="false" %>
<%@ attribute name="selected" type="java.lang.String" required="false" %>


<script src="/javascript/passwordmeter.js"></script>
<script src="/javascript/profile-edit.js"></script>

<link rel=stylesheet type="text/css" href="/css/tabEdit.css">


<div id='personEdit'>
    <ul class="tabs">
        <li>
            <a href="#information" ${empty person.email and person.emailList ? 'style="color: red;"' : '' }>Information</a>
        </li>
        <li>
            <a href="#biography">Biography</a>
        </li>
        <li>
            <a href="#publications">Publications</a>
        </li>
        <li><a href="#login">Login</a></li>
        <li>
            <a href="#picture" ${empty person.snapshot ? 'style="color: red;"' : '' }>Picture</a>
        </li>
    </ul>


    <div class='panes'>
        <div id='information'>
            ${person.emailList and empty person.email ? '<div style="color: red;">Please provide a valid email if on distribution list.</div>' : '' }
            <form:form method="post" commandName="<%=LookupStrings.FORM_BEAN%>"
                       action="/action/profile/person/edit/${person.zdbID}" enctype="multipart/form-data"
                       cssClass="edit-box mark-dirty" id="person-edit-information">

                <table>
                    <tr>
                        <td>
                            <form:label cssClass="information-first-field" path="firstName">First Name:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="firstName"/>
                            <zfin2:errors errorResult="${errors}" path="firstName"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="lastName">Last Name:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="lastName"/>
                            <zfin2:errors errorResult="${errors}" path="lastName"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="phone">Phone:</form:label>
                        </td>
                        <td>
                            <form:input path="phone"/>
                            <zfin2:errors errorResult="${errors}" path="phone"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="fax">Fax:</form:label>
                        </td>
                        <td>
                            <form:input path="fax"/>
                            <zfin2:errors errorResult="${errors}" path="fax"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="email">Email:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="email"/>
                            <zfin2:errors errorResult="${errors}" path="email"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="url">URL:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="url"/>
                            <zfin2:errors errorResult="${errors}" path="url"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="orcidID">Orcid Id:</form:label>
                        </td>
                        <td>
                            <form:input size="19" path="orcidID"/>
                            <zfin2:errors errorResult="${errors}" path="orcidID"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label cssStyle="vertical-align: top;" path="address">Address:</form:label>
                        </td>
                        <td>
                            <form:textarea path="address" rows="5" cols="80"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="country">Country:</form:label>
                        </td>
                        <td>
                            <form:select path="country">
                                <form:option value="" />
                                <form:options items="${countryList}" />
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="emailList">Email List:</form:label>
                        </td>
                        <td>
                            <form:checkbox size="50" path="emailList"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="deceased">Deceased:</form:label>
                        </td>
                        <td>
                            <form:checkbox size="50" path="deceased"/>
                        </td>
                    </tr>
                </table>
                <br>


                <input type="submit" value="Save"/>
                <a href="/action/profile/view/${person.zdbID}">Cancel</a>


            </form:form>
        </div>
        <div id="biography">
            <form:form method="post" commandName="<%=LookupStrings.FORM_BEAN%>"
                       action="/action/profile/person/update-biography/${person.zdbID}" enctype="multipart/form-data"
                       id="person-edit-biography" cssClass="mark-dirty"
                    >
                <%--<div align="left">--%>
                <form:label
                        path="personalBio">Biography and Research Interests:</form:label>
                <br>
                <%--<form:textarea cols="80" rows="10" htmlEscape="false" path="personalBio"/>--%>
                <form:textarea cssClass="biography-first-field" cols="80" rows="10" path="personalBio"/>
                <zfin2:errors errorResult="${errors}" path="personalBio"/>
                <br>
                <%--</div>--%>
                <input type="submit" value="Save"/>
                <a href="/action/profile/view/${person.zdbID}">Cancel</a>

            </form:form>

        </div>
        <div id="publications">
            <form:form method="post" commandName="<%=LookupStrings.FORM_BEAN%>"
                       action="/action/profile/person/update-publications/${person.zdbID}" enctype="multipart/form-data"
                       id="person-edit-publications" cssClass="mark-dirty"
                    >
                <form:label path="nonZfinPublications">Non-Zebrafish Publications:</form:label> <br>
                <form:textarea cssClass="publications-first-field" cols="80" rows="10" path="nonZfinPublications"/>
                <zfin2:errors errorResult="${errors}" path="nonZfinPublications"/>

                <p style="font-size: small;">If you would like to include a Zebrafish publication please contact
                    <a href="mailto:<%=ZfinProperties.getAdminEmailAddresses()[0]%>">ZFIN</a>.<br/>
                </p>
                <br/>
                <input type="submit" value="Save"/>
                <a href="/action/profile/view/${person.zdbID}">Cancel</a>

            </form:form>
        </div>


        <div id='login'>
            <%--<table>--%>
            <%--<tr valign="top">--%>
            <%--<td rowspan="2">--%>
            <c:set var='secure' value="<%=ZfinPropertiesEnum.SECURE_HTTP.toString()%>"/>
            <c:set var='domain' value="<%=ZfinPropertiesEnum.DOMAIN_NAME.toString()%>"/>
            <c:set var='secureServer' value="${secure}${domain}"/>
            <%--${secureServer}--%>
            <%--action="/action/profile/person/edit-user-details/${person.zdbID}" enctype="multipart/form-data"--%>
            <form:form method="post" commandName="<%=LookupStrings.FORM_BEAN%>"
                       action="${secureServer}/action/profile/person/edit-user-details/${person.zdbID}"
                       enctype="multipart/form-data" id="person-edit-login" cssClass="mark-dirty"
                    >
                <form:label path="accountInfo.login">Login:</form:label>
                <form:input cssClass="login-first-field" size="50" path="accountInfo.login"/>
                <zfin2:errors errorResult="${errors}" path="accountInfo.login"/>

                <div>
                    <form:label path="accountInfo.pass1">Password:</form:label>
                    <form:password size="50" path="accountInfo.pass1" cssClass="fill-with-generated-password"
                                   onkeyup="testPassword(document.getElementById('accountInfo.pass1').value,'passwordScore','passwordVerdict');"/>
                    <zfin2:errors errorResult="${errors}" path="accountInfo.pass1"/>
                </div>

                <div>
                    <form:label path="accountInfo.pass2">Repeat Password:</form:label>
                    <form:password size="50" path="accountInfo.pass2" cssClass="fill-with-generated-password"/>
                    <zfin2:errors errorResult="${errors}" path="accountInfo.pass2"/>
                </div>

                <input type="button" id="generate-password-button" value="generate password"/>
                <span class="fill-with-generated-password"></span>
                <script>
                    jQuery('#generate-password-button').click(function () {
                        generatePassword('fill-with-generated-password');
                    });
                </script>

                <div>Password Strength: <strong><span id="passwordVerdict"></span></strong></div>
                <br>
                <%--prevents changing your own accidentally --%>
                <c:choose>
                    <c:when test="${not empty securityPersonZdbID and person.zdbID ne securityPersonZdbID}">
                        <form:label path="accountInfo.role">Privileges:</form:label>
                        <form:select path="accountInfo.role" multiple="false">
                            <form:option value="submit"/>
                            <form:option value="root"/>
                        </form:select>
                    </c:when>
                    <c:otherwise>
                        <form:hidden path="accountInfo.role"/>
                        <c:if test="${person.accountInfo.root}">
                            <div>
                                <form:label path="accountInfo.role">Privileges: </form:label>
                                <span style="color: gray;"> ${person.accountInfo.role} </span>
                            </div>
                        </c:if>
                    </c:otherwise>
                </c:choose>


                <br><br>

                <input type="submit" value="Save"/>
                <a href="/action/profile/view/${person.zdbID}">Cancel</a>

                <%--</div>--%>
            </form:form>
        </div>

        <div id='picture'>
            <zfin2:editSnapshot value='${person}'/>
        </div>
    </div>
</div>

<script type="text/javascript">

    jQuery('.tabs a').tabbify('.panes > div');

</script>


