<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<H2>Request an Anatomical Structure</H2>

We welcome your input and comments. Please use this form to submit a new anatomical structure to ZFIN.
We appreciate as much detail as possible and references as appropriate, such as
a proposed definition, stage information, what it is part of, what it develops from or into.

Please be sure to include your contact information in the fields below. We will review your comments promptly.

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.RequestNewAnatomyTermBean" scope="request"/>

<form:form action="request-new-anatomy-term-submit" commandName="formBean" method="post">


    <input type=hidden name="subject" VALUE="Request a new Anatomical Structure">

    <H3>From</H3>
    <TABLE border=0>
        <TR>
            <TD>*First Name</TD>
            <TD><form:input path="firstName" size="30"></form:input></TD>
            <TD><form:errors path="firstName" cssClass="error indented-error"/></TD>
        </TR>
        <TR>
            <TD>*Last Name</TD>
            <TD><form:input path="lastName" size="30"></form:input></TD>
            <TD><form:errors path="lastName" cssClass="error indented-error"/></TD>
        </TR>
        <TR>
            <TD><font color="#FFFFFF">*</font>Institution</TD>
            <TD><form:input path="institution" size="30"></form:input></TD>
        </TR>
        <TR>
            <TD>*E-mail address</TD>
            <TD><form:input path="email" size="30"></form:input></TD>
            <TD><form:errors path="email" cssClass="error indented-error"/></TD>
        </TR>
    </TABLE>

    <H3>*Description:</H3>
    <form:textarea path="termDetail" cols="60" rows="8"></form:textarea>
    <div><form:errors path="termDetail" cssClass="error indented-error"/></div>
    <hr>

    <font size=-1>*Required Fields</font>

    <br>
    <input type=submit value=Submit>
    <input type=reset>
    <input type=button value="Cancel" onClick="window.close()">
</form:form>