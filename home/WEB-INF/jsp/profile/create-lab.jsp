<%@ page import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <zfin2:errors errorResult="${errors}"/>



    <form:form method="post" commandName="${LookupStrings.FORM_BEAN}"
               action="/action/profile/lab/create" enctype="multipart/form-data"
               style="border: 2px solid gray;   background-color: #FEF7D6; "
            >

        <table width="80%">
            <tr>
                <td>
                    <div style="align: left; width: 60%;">
                        <form:label path="name">Name:</form:label>
                        <form:input size="50" path="name"/>
                        <form:errors path="name" cssClass="error-inline" />
                        <br>
                        <form:label path="phone">Phone:</form:label>
                        <form:input path="phone"/>
                        <form:errors path="phone" cssClass="error-inline"/>
                        <br>
                        <form:label path="fax">Fax:</form:label>
                        <form:input path="fax"/>
                        <form:errors path="fax" cssClass="error-inline"/>
                        <br>
                        <form:label path="email">Email:</form:label>
                        <form:input size="50" path="email"/>
                        <form:errors path="email" cssClass="error-inline"/>
                        <br>
                        <form:label path="url">URL:</form:label>
                        <form:input size="50" path="url"/>
                        <form:errors path="url" cssClass="error-inline"/>
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

    <script>
        $(function() {
            $('#formBean').on('submit', function () {
                $(this).find('input[type="submit"]').attr('disabled', 'disabled');
            });
        });
    </script>
</z:page>
