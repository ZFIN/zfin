<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<form:form action="/action/orthology/save-note/${formBean.geneID}" method="POST" name="create-new-orthology-note" commandName="formBean">
    <label class="indented-label">Note:</label>
    <p/>
    <form:textarea path="note" cols="60" rows="5"></form:textarea>
    <form:errors path="note" cssClass="error indented-error"/>
    <p/>
    <form:hidden path="geneID"/>
    <input type=submit name="new note" value="Save Note">
    </p>
</form:form>
