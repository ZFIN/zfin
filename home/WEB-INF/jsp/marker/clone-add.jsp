<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%--<authz:authorize>--%>
<form:form commandName="formBean"  >
    <form:hidden path="ownerZdbID"/>
    <table>
        <tr>
            <td>
                Clone Name:
            </td>
            <td align="left">
                <form:input path="name"/>
            </td>
            <td>
                <form:errors path="name"  cssClass="error"/>
            </td>
        </tr>
        <tr>
            <td>
                Type:
            </td>
            <td align="left">
                <form:select path="markerType" items="${cloneMarkerTypes}"/>
                <form:errors path="markerType"  cssClass="error"/>
            </td>
        </tr>
        <tr>
            <td>
                Library:
            </td>
            <td align="left">
                <form:select itemLabel="name" itemValue="zdbID" path="libraryZdbID" items="${cloneLibraries}"/>
                <form:errors path="libraryZdbID"  cssClass="error"/>
            </td>
        </tr>
    </table>

    <hr>
    <script type="text/javascript">
        function validateAndSubmit(){
            if( formBean.name.value == ''){
                alert("Please enter name.") ;
            }
            else{
                formBean.submit();
            }
        }
    </script>

    <input type="button" value="Create Clone and Edit" onclick="validateAndSubmit();">
    <input type="reset">
    <br><br>
    <form:errors cssClass="error"/>
</form:form>
<%--</authz:authorize>--%>
