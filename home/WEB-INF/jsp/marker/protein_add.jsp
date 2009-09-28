<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%--<authz:authorize>--%>
<form:form commandName="formBean" >
    <form:hidden path="ownerZdbID"/>
<table>
    <tr>
        <td>
            Protein Accession:
        </td>
        <td align="left">
            ${formBean.name}
        </td>
    </tr>
    <tr>
        <td>
            Sequence:
        </td>
    </tr>
    <tr>
        <td>
            <textarea rows="4" cols="60"/>
            <br>
            <input type="button" name="save"/>
        </td>
    </tr>
</table>

<hr>
<input type="submit" value="Create Clone and Edit">
<input type="reset">
<br><br>
<form:errors cssClass="error"/>
</form:form>
    <%--</authz:authorize>--%>
