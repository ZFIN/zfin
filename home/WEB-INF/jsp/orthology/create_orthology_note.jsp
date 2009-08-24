<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<form:form method="POST" name="create-new-orthology-note">
    <label class="indented-label">Note:</label>
    <p/>
    <form:textarea path="note" cols="60" rows="5"></form:textarea>
    <form:errors path="note" cssClass="error indented-error"/>
    <p/>
    <input type=submit name="new note" value="Submit new Note">
    </p>
</form:form>
