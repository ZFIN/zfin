<%@ page import="org.zfin.marker.presentation.DisruptorAddBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.DisruptorAddBean" scope="request"/>

<html>

<h1>Describe new ${formBean.disruptorType}</h1>

<form:form action="disruptor-do-submit?disruptorType=${formBean.disruptorType}" commandName="formBean" method="post">
    <div>
        <form:label path="<%= DisruptorAddBean.NEW_DISRUPTOR_NAME%>" class="curation-form-label">${formBean.disruptorType} name:</form:label>
        <form:input path="<%= DisruptorAddBean.NEW_DISRUPTOR_NAME%>" size="80"
                    onkeypress="return noenter(event)"></form:input>
        <form:errors path="<%= DisruptorAddBean.NEW_DISRUPTOR_NAME%>" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
        <form:label path="<%= DisruptorAddBean.NEW_DISRUPTOR_ALIAS%>" class="curation-form-label">${formBean.disruptorType} alias:</form:label>
        <form:input onkeypress="return noenter(event)" path="<%= DisruptorAddBean.NEW_DISRUPTOR_ALIAS%>" size="50"></form:input>
        <form:errors path="<%= DisruptorAddBean.NEW_DISRUPTOR_ALIAS%>" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
        <form:label path="<%= DisruptorAddBean.NEW_DISRUPTOR_COMMENT%>" class="curation-form-label">Note:</form:label>
    </div>
    <div>
        <form:textarea path="<%= DisruptorAddBean.NEW_DISRUPTOR_COMMENT%>" rows="5" cols="50" />
        <form:errors path="<%= DisruptorAddBean.NEW_DISRUPTOR_COMMENT%>" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
        <link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
        <script language="javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>
        <b>Add Target Gene:</b>
        <div id="targetgene" onkeypress="return noenter(event)"></div>
    </div>
    <p/>
    <table border=0>
        <tr>
            <td valign=top>
                <label for="sequence" class="namesearchLabel">Sequence:</label>
                <c:if test="${formBean.disruptorType ne 'TALEN'}">
                    <input type=button value="Reverse" onClick="reverseSequence();">
                    <input type=button value="Complement" onClick="complementSequence();">
                    <input type=button value="Reverse & Complement" onClick="reverseComplementSequence();">
                </c:if>
            </td>
            <td width=20></td><!-- spacer column -->
            <td><form:label path="<%= DisruptorAddBean.NEW_DISRUPTOR_CURNOTE%>" class="curation-form-label">Curator Note:</form:label></td>
        </tr>
        <tr>
            <td>
                <c:if test="${formBean.disruptorType ne 'TALEN'}">
                    Reported: 5'-   <form:input id="reportSeq" path="<%= DisruptorAddBean.NEW_DISRUPTOR_REPORTEDSEQUENCE%>" name="reportSeq" size="50" onChange="this.value = this.value.toUpperCase()" onkeypress="return noenter(event)"></form:input> -3'
                </c:if>
                <c:if test="${formBean.disruptorType eq 'TALEN'}">
                    Sequence 1:&nbsp;&nbsp;  5'- <form:input id="displaySeq" name="displaySeq" path="<%= DisruptorAddBean.NEW_DISRUPTOR_SEQUENCE%>" size="50" onChange="this.value = this.value.toUpperCase()" onkeypress="return noenter(event)"></form:input> -3'
                    <form:errors path="<%= DisruptorAddBean.NEW_DISRUPTOR_SEQUENCE%>" cssClass="error indented-error"/>
                </c:if>
            </td>
            <td width=20></td><!-- spacer column -->
            <td valign=top rowspan=2>
                <form:textarea id="curatorNote" path="<%= DisruptorAddBean.NEW_DISRUPTOR_CURNOTE%>" rows="5" cols="70" />
                <form:errors path="<%= DisruptorAddBean.NEW_DISRUPTOR_CURNOTE%>" cssClass="error indented-error"/>
            </td>
        </tr>
        <tr>
            <td>
                <c:if test="${formBean.disruptorType ne 'TALEN'}">
                    Displayed: 5'- <form:input id="displaySeq" name="displaySeq" path="<%= DisruptorAddBean.NEW_DISRUPTOR_SEQUENCE%>" size="50" onChange="this.value = this.value.toUpperCase()" onkeypress="return noenter(event)"></form:input> -3'
                    <form:errors path="<%= DisruptorAddBean.NEW_DISRUPTOR_SEQUENCE%>" cssClass="error indented-error"/>
                </c:if>
                <c:if test="${formBean.disruptorType eq 'TALEN'}">
                    Sequence 2:&nbsp;&nbsp;  5'- <form:input id="displaySeq" name="displaySeq" path="<%= DisruptorAddBean.NEW_DISRUPTOR_SECOND_SEQUENCE%>" size="50" onChange="this.value = this.value.toUpperCase()" onkeypress="return noenter(event)"></form:input> -3'
                    <form:errors path="<%= DisruptorAddBean.NEW_DISRUPTOR_SECOND_SEQUENCE%>" cssClass="error indented-error"/>
                </c:if>
            </td>
        </tr>
    </table>
    <p/>
    <div>
        <form:label path="<%= DisruptorAddBean.DISRUPTOR_PUBLICATION_ZDB_ID%>" class="curation-form-label">Publication:</form:label>
        <form:input path="<%= DisruptorAddBean.DISRUPTOR_PUBLICATION_ZDB_ID%>" size="25"
                    onkeypress="return noenter(event)" value="${formBean.disruptorPublicationZdbID}"></form:input>
        <form:errors path="<%= DisruptorAddBean.DISRUPTOR_PUBLICATION_ZDB_ID%>" cssClass="error indented-error"/>
    </div>
    <p/>
    <c:if test="${formBean.disruptorType eq 'TALEN'}">
        <input type=submit name=s_new value="Submit new ${formBean.disruptorType}" onclick="warnAboutNoTargetGene()">
    </c:if>
    <c:if test="${formBean.disruptorType ne 'TALEN'}">
        <input type=submit name=s_new value="Submit new ${formBean.disruptorType}" onclick="populateDispSeq()">
    </c:if>
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

    var LookupProperties0 = {
        divName: "targetgene",
        inputName: "targetGeneSymbol",
        showError: true,
        wildcard: false,
        width: "25",
        type: "GENEDOM_AND_EFG_LOOKUP",
        useTermTable: false
    };


    function baseComplement(s) {
        var i;
        var complemented = "";

        // Search through string's characters one by one
        // For each base, return the complementary base. true.

        for (i = 0; i < s.length; i++) {
            if (s.charAt(i) == 'A') {
                complemented = complemented + "T";
            }
            else if (s.charAt(i) == 'T') {
                complemented = complemented + "A";
            }
            else if (s.charAt(i) == 'C') {
                complemented = complemented + "G";
            }
            else if (s.charAt(i) == 'G') {
                complemented = complemented + "C";
            }
        }

        return complemented;
    }

    // Returns the characters of sequence string in reverse order
    function reverseString (s) {
        var i;
        var reversed = "";

        // Walk the input string backwards

        for (i = s.length-1; i >= 0; i--) {
            // Append the character at position i to the reversed string;
            reversed = reversed + s.charAt(i);
        }

        return reversed;
    }

    function reverseSequence() {
        var displayedSeq = document.getElementById('displaySeq');
        var reportSeq = document.getElementById('reportSeq');
        var revSeq;
        revSeq = reverseString(reportSeq.value);

        displayedSeq.value = revSeq;

        doCuratorNote('reversed');
    }

    function complementSequence() {
        var displayedSeq = document.getElementById('displaySeq');
        var reportSeq = document.getElementById('reportSeq');
        var compSeq;
        compSeq = baseComplement(reportSeq.value);

        displayedSeq.value = compSeq;

        doCuratorNote('complemented');
    }


    function reverseComplementSequence() {
        var displayedSeq = document.getElementById('displaySeq');
        var reportSeq = document.getElementById('reportSeq');
        var revSeq;
        revSeq = reverseString(reportSeq.value);
        var compSeq;
        compSeq = baseComplement(revSeq);

        displayedSeq.value = compSeq;

        doCuratorNote('reversed and complemented');
    }

    function doCuratorNote(action) {
        var cNote = document.getElementById('curatorNote');
        var reportSeq = document.getElementById('reportSeq');
        cNote.value = "Reported Sequence: "+ reportSeq.value + " was " + action +".\r\n" + cNote.value;
    }

    function populateDispSeq() {
        var displayedSeq = document.getElementById('displaySeq');
        var reportSeq = document.getElementById('reportSeq');
        if(!displayedSeq.value || displayedSeq.value.length == 0 || !/^[\s]+$/.test(displayedSeq.value)) {
            displayedSeq.value = reportSeq.value;
        }

        warnAboutNoTargetGene();
    }

    function warnAboutNoTargetGene() {
        var targetGene = document.getElementById("targetGeneSymbol");
        if (!targetGene || !targetGene.value) {
            alert("Add target gene!");
        }
    }


</script>


