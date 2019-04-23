<%@ page import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:errors errorResult="${errors}"/>


<script type="text/javascript" src="/javascript/passwordmeter.js"></script>
<script src="/javascript/dist/profiles.bundle.js"></script>

<form:form method="post" commandName="<%=LookupStrings.FORM_BEAN%>"
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
                    <form:input size="50" path="email"/>
                    <zfin2:errors errorResult="${errors}" path="email"/>
                    <br>
                    <form:label path="pass1">Password:</form:label>
                    <form:input size="50" path="pass1" cssClass="fill-with-generated-password"
                                onkeyup="testPassword(document.getElementById('pass1').value,'passwordScore','passwordVerdict');"/>
                    <zfin2:errors errorResult="${errors}" path="pass1"/>
                    <br>
                    <input type="button" id="generate-password-button" value="generate password"/>

                   <div> Password Strength:<strong><span id="passwordVerdict"></span></strong></div>
                    <br>
                    <form:label path="firstName">First Name:</form:label>
                    <form:input size="50" path="firstName"/>
                    <zfin2:errors errorResult="${errors}" path="firstName"/>
                    <br>
                    <form:label path="lastName">Last Name:</form:label>
                    <form:input size="50" path="lastName"/>
                    <zfin2:errors errorResult="${errors}" path="lastName"/>
                    <br>
                    <%--<form:label path="middleNameOrInitial">Middle Name or Initial:</form:label>--%>
                    <%--<form:input size="50" path="middleNameOrInitial"/>--%>
                    <%--<zfin2:errors errorResult="${errors}" path="middleNameOrInitial"/>--%>
                    <%--<br>--%>
                    <form:label path="emailList">On Email List:</form:label>
                    <form:checkbox size="50" path="emailList"/>
                    <br>
                    
                    <c:if test="${!empty organization}">
                        Position: 
                        <c:forEach var="position" items="${positions}">
                            <div style="text-indent: 2em;">
                                <form:radiobutton path="position" id="position-${position.id}" value="${position.id}"/>
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

