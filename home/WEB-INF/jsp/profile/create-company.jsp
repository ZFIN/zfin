<%@ page import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:errors errorResult="${errors}"/>



<form:form method="post" commandName="${LookupStrings.FORM_BEAN}"
           action="/action/profile/company/create" enctype="multipart/form-data"
           style="border: 2px solid gray;   background-color: #FEF7D6; "
        >

    <table width="80%">
        <tr>
            <td>
                <div style="align: left; width: 60%;">
                    <form:label path="name">Name:</form:label>
                    <form:input size="50" path="name"/>
                    <zfin2:errors errorResult="${errors}" path="name"/>
                    <br>
                    <form:label path="phone">Phone:</form:label>
                    <form:input path="phone"/>
                    <zfin2:errors errorResult="${errors}" path="phone"/>
                    <br>
                    <form:label path="fax">Fax:</form:label>
                    <form:input path="fax"/>
                    <zfin2:errors errorResult="${errors}" path="fax"/>
                    <br>
                    <form:label path="email">Email:</form:label>
                    <form:input size="50" path="email"/>
                    <zfin2:errors errorResult="${errors}" path="email"/>
                    <br>
                    <form:label path="url">URL:</form:label>
                    <form:input size="50" path="url"/>
                    <zfin2:errors errorResult="${errors}" path="url"/>
                    <br>
                </div>

            </td>
        </tr>
    </table>

    <input type="submit" value="Create"/>
    <input type="button" value="Cancel"
           onclick="window.location.href = '/' ; "
            />

</form:form>

