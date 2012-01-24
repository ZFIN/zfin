<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<SCRIPT type="text/javascript">
    function isBlank(s)
    {
        var len = s.length
        if (len == 0)
            return true

        var i
        for (i = 0; i < len; i++)
        {
            if (s.charAt(i) != " ") return false
        }

        return true
    }

    function valid(fieldname, fieldvalue)
    {
        if (isBlank(fieldvalue))
        {
            alert(fieldname + " cannot be left blank.")
            return false
        }
        return true
    }

    function validateForm()
    {
        if (!valid("First Name", document.input.firstname.value))
            return false

        if (!valid("Last Name", document.input.lastname.value))
            return false

        if (!valid("Email", document.input.email.value))
            return false

        if (!valid("Comments", document.input.comments.value))
            return false

        return true;
    }

</SCRIPT>

<H2>Request an Anatomical Structure</H2>

We welcome your input and comments. Please use this form to submit a new anatomical structure to ZFIN.
We appreciate as much detail as possible and references as appropriate, such as
a proposed definition, stage information, what it is part of, what it develops from or into.

Please be sure to include your contact information in the fields below. We will review your comments promptly.

<form:form onsubmit="return validateForm()" action="request-new-anatomy-term-submit">


    <input type=hidden name="subject" VALUE="Request a new Anatomical Structure">

    <H3>From</H3>
    <TABLE border=0>
        <TR>
            <TD>*First Name</TD>
            <TD><INPUT TYPE=text NAME="firstname" SIZE=30></TD>
        </TR>
        <TR>
            <TD>*Last Name</TD>
            <TD><INPUT TYPE=text NAME="lastname" SIZE=30></TD>
        </TR>
        <TR>
            <TD><font color="#FFFFFF">*</font>Institution</TD>
            <TD><INPUT TYPE=text NAME="institution" SIZE=30></TD>
        </TR>
        <TR>
            <TD>*E-mail address</TD>
            <TD><INPUT TYPE=text NAME="email" SIZE=30></TD>
        </TR>
    </TABLE>

    <H3>*Description:</H3>
    <textarea name="termDetail" cols=60 rows=8 ></textarea>
    <hr>

    <font size=-1>*Required Fields</font>

    <br>
    <input type=submit value=Submit>
    <input type=reset>
    <input type=button value="Cancel" onClick="window.close()">
</form:form>