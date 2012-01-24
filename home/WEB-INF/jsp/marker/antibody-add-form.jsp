<%@ page import="org.zfin.marker.presentation.CreateAntibodyFormBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<html>
<form:form action="antibody-do-submit" commandName="formBean" method="post">
    <label for="antibodyName" class="indented-label">Antibody name:</label>
    <form:input path="<%= CreateAntibodyFormBean.NEW_AB_NAME%>" size="25"
                onkeypress="return noenter(event)"></form:input>
    <form:errors path="<%= CreateAntibodyFormBean.NEW_AB_NAME%>" cssClass="error indented-error"/>
    <p>
        <label for="antibodyPublicationZdbID" class="indented-label">Publication:</label>
            <form:input path="<%= CreateAntibodyFormBean.AB_PUBLICATION_ZDB_ID%>" size="25"
                        onkeypress="return noenter(event)"></form:input>
            <form:errors path="<%= CreateAntibodyFormBean.AB_PUBLICATION_ZDB_ID%>" cssClass="error indented-error"/>

    <p>
        <input type=submit name=s_new value="Submit new Antibody">
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
