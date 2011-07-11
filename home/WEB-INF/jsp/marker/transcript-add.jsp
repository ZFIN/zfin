<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<form:form commandName="formBean" >
    <table>
        <tr>
            <td>Transcript Name:</td>
            <td><form:input path="name"/> </td>
            <td> <form:errors path="name"  cssClass="error"/> </td>
        </tr>
        <tr>
            <td>Type:</td>
            <td><form:select path="chosenType" items="${types}"/></td>
            <td> <form:errors path="chosenType"  cssClass="error"/> </td>
        </tr>
        <tr>
            <td>Status:</td>
            <td>
                <form:select path="chosenStatus">
                    <c:forEach var="availableStatus" items="${statuses}">
                        <c:if test="${!empty availableStatus.key}">
                        <form:option value="${availableStatus.value}" label="${availableStatus.key}"/>
                        </c:if>
                    </c:forEach>
                </form:select>
            </td>
            <td> <form:errors path="chosenStatus"  cssClass="error"/> </td>
        </tr>
    </table>

    <input type="submit" value="Create Transcript and Edit">
    <input type="reset">
    <br><br>
    <form:errors cssClass="error"/>
</form:form>
