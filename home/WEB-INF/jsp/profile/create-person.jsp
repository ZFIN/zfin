<%@ page import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <zfin2:errors errorResult="${errors}"/>

    <script src="${zfn:getAssetPath("profiles.js")}"></script>

    <c:if test="${prefillFailed}">
        <div style="border: 2px solid #a00; padding: 0.5em; margin-bottom: 0.5em;">
            That account request could not be loaded, so this form has not been prefilled. Please
            enter the details from the request email by hand.
        </div>
    </c:if>

    <c:if test="${!empty submission}">
        <div style="border: 2px solid gray; padding: 0.5em; margin-bottom: 0.5em; background-color: #EFEFEF;">
            <strong>Prefilled from the account request of
                <fmt:formatDate value="${submission.date}" pattern="dd MMM yyyy"/></strong>
            <p>The form below is filled in from the request. These were also submitted, and this
                form does not set them &mdash; add lab membership and position after the account
                exists:</p>
            <table>
                <tr><td>Lab:</td><td><c:out value="${submission.lab}"/></td></tr>
                <tr><td>Role/Position:</td><td><c:out value="${submission.role}"/></td></tr>
                <tr><td>Comments:</td><td><c:out value="${submission.comments}"/></td></tr>
            </table>
        </div>
    </c:if>

    <form:form method="post" modelAttribute="${LookupStrings.FORM_BEAN}"
               action="/action/profile/person/create" enctype="multipart/form-data"
               style="border: 2px solid gray;   background-color: #FEF7D6; "
            >

        <table width="80%">
            <tr>
                <td>
                    <div style="align: left; width: 60%;">
                        (Leave blank to use email)
                        <form:label path="putativeLoginName">Login:</form:label>
                        <form:input size="50" path="putativeLoginName"/>
                        <zfin2:errors errorResult="${errors}" path="putativeLoginName"/>
                        <br>
                        <form:label path="email">Email:</form:label>
                        <form:input size="50" path="email" required="true"/>
                        <zfin2:errors errorResult="${errors}" path="email"/>
                        <br>
                        <form:label path="pass1">Password:</form:label>
                        <form:input size="50" path="pass1" cssClass="fill-with-generated-password"
                                    required="true"
                                    onkeyup="testPassword(document.getElementById('pass1').value,'passwordScore','passwordVerdict');"/>
                        <zfin2:errors errorResult="${errors}" path="pass1"/>
                        <br>
                        <input type="button" id="generate-password-button" value="generate password"/>

                       <div> Password Strength:<strong><span id="passwordVerdict"></span></strong></div>
                        <br>
                        <form:label path="firstName">First Name:</form:label>
                        <form:input size="50" path="firstName" required="true"/>
                        <zfin2:errors errorResult="${errors}" path="firstName"/>
                        <br>
                        <form:label path="lastName" required="true">Last Name:</form:label>
                        <form:input size="50" path="lastName"/>
                        <zfin2:errors errorResult="${errors}" path="lastName"/>
                        <br>
                        <%--<form:label path="middleNameOrInitial">Middle Name or Initial:</form:label>--%>
                        <%--<form:input size="50" path="middleNameOrInitial"/>--%>
                        <%--<zfin2:errors errorResult="${errors}" path="middleNameOrInitial"/>--%>
                        <%--<br>--%>
                        <form:label path="orcidID">ORCID iD:</form:label>
                        <form:input size="50" path="orcidID"/>
                        <zfin2:errors errorResult="${errors}" path="orcidID"/>
                        <br>
                        <form:label path="phone">Phone:</form:label>
                        <form:input size="50" path="phone"/>
                        <zfin2:errors errorResult="${errors}" path="phone"/>
                        <br>
                        <form:label path="address">Address:</form:label>
                        <form:input size="50" path="address"/>
                        <zfin2:errors errorResult="${errors}" path="address"/>
                        <br>
                        <form:label path="country">Country:</form:label>
                        <form:select path="country">
                            <form:option value=""/>
                            <form:options items="${countryList}"/>
                        </form:select>
                        <zfin2:errors errorResult="${errors}" path="country"/>
                        <br>
                        <form:label path="url">Website:</form:label>
                        <form:input size="50" path="url"/>
                        <zfin2:errors errorResult="${errors}" path="url"/>
                        <br>
                        <form:label path="emailList">On Email List:</form:label>
                        <form:checkbox size="50" path="emailList"/>
                        <br>

                        <c:if test="${!empty organization}">
                            Position:
                            <c:forEach var="position" items="${positions}">
                                <div style="text-indent: 2em;">
                                    <form:radiobutton path="position" id="position-${position.id}" value="${position.id}" required="true"/>
                                    <label for="position-${position.id}">${position.name}</label>
                                </div>
                            </c:forEach>
                            <div style="text-indent: 4em;"> within: <zfin:link entity="${organization}"/></div>

                            <form:hidden path="organizationZdbId" value="${organization.zdbID}"/>
                        </c:if>

                    </div>

                </td>
            </tr>
        </table>

        <input type="submit" value="Create"/>
        <input type="button" value="Cancel"
               onclick="window.location.href = '/' ; "
                />

    </form:form>
</z:page>
