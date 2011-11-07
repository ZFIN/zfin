<%@ page import="org.zfin.feature.presentation.CreateAlleleDesignationFormBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<html>
<form:form  commandName="formBean" method="post">
    <label for="lineDesig" class="indented-label">Company/Lab Line Designation:</label>
    <form:input path="<%= CreateAlleleDesignationFormBean.NEW_LINE_DESIG%>" size="25"
                onkeypress="return noenter(event)"></form:input>
    <form:errors path="<%= CreateAlleleDesignationFormBean.NEW_LINE_DESIG%>" cssClass="error indented-error"/>
    <p>
        <label for="lineLocation" class="indented-label">Institution/Company:</label>
            <form:input path="<%= CreateAlleleDesignationFormBean.NEW_LINE_LOCN%>" size="25"
                        onkeypress="return noenter(event)"></form:input>
            <form:errors path="<%= CreateAlleleDesignationFormBean.NEW_LINE_LOCN%>" cssClass="error indented-error"/>

    <p>
        <input type=submit name=s_new value="Submit new LineDesignation">
    </p>

</form:form>

</html>

<script type="text/javascript">

    function noenter(e) {
        var ENTER_KEY = 13;
        var code = "";

        if (window.event) // IE
        {
            code = e.keyCode;
        }
        else if (e.which) // Netscape/Firefox/Opera
        {
            code = e.which;
        }

        if (code == ENTER_KEY) {
            return false;
        }
    }
</script>
