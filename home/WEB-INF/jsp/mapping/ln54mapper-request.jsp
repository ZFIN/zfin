<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<form:form commandName="formBean">

    <h3>Request LN54 Mapping</h3>
    <p>
    Please use this form to submit vector scores for placement on the LN54 radiation hybrid panel. We will attempt to return a mapping location within 2 business days.  Graphics will not be available.
    </p>

    <p>
    Please be sure to include your contact information in the fields below.  
    Questions may be sent to <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a>.
    </p>

    <table>
        <tr>
            <td align="right" width=20%>*Your Name:</td>
            <td> <form:input path="name"/></td>
            <td> <form:errors path="name" cssClass="error" /></td>
        </tr>

        <tr>
            <td align="right">*Your Email:</td>
            <td> <form:input path="email"/></td>
            <td> <form:errors path="email" cssClass="error" /></td>
        </tr>

        <tr>
            <td align="right">*Scoring Vector:</td>
            <td colspan=2> <form:input size="93" path="scoringVector" />
            <br>
<font size=-1>Sequence must be exactly 93 characters with only the characters '0', '1', and '2' allowed.</font>
            </td>
        </tr>
        <tr>
            <td colspan=2></td><td> <form:errors path="scoringVector" cssClass="error" /></td>
        </tr>
        <tr>
            <td align="right">Marker Name:</td>
            <td> <form:input path="markerName"/></td>
            <td> <form:errors path="markerName" cssClass="error" /></td>
        </tr>
        <tr><td>&nbsp;</td></tr>

        <tr>
            <td colspan=3>
                <font size=-1>*Required Fields</font> <br>
                <input type=submit value=Submit> <input type=reset>
            </td>
        </tr>
    </table>

</form:form>


