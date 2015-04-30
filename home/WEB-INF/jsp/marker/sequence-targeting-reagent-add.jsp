<%@ page import="org.zfin.marker.presentation.SequenceTargetingReagentAddBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentAddBean" scope="request"/>

<html>

<script src="/javascript/profile-edit.js"></script>

<link rel=stylesheet type="text/css" href="/css/tabEdit.css">

<h1>Describe new ${formBean.sequenceTargetingReagentType}</h1>

<c:choose>
    <c:when test="${formBean.sequenceTargetingReagentType eq 'Morpholino'}">
        <c:set var="seqBoxSize">50</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="seqBoxSize">60</c:set>
    </c:otherwise>
</c:choose>


<form:form action="sequence-targeting-reagent-do-submit?sequenceTargetingReagentType=${formBean.sequenceTargetingReagentType}" commandName="formBean" method="post">
    <div>
        <form:label path="<%= SequenceTargetingReagentAddBean.NEW_STR_NAME%>" class="curation-form-label">${formBean.sequenceTargetingReagentType} name:</form:label>
        <form:input path="<%= SequenceTargetingReagentAddBean.NEW_STR_NAME%>" size="80"
                    onkeypress="return noenter(event)"></form:input>
        <form:errors path="<%= SequenceTargetingReagentAddBean.NEW_STR_NAME%>" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
        <form:label path="<%= SequenceTargetingReagentAddBean.NEW_STR_ALLIAS%>" class="curation-form-label">${formBean.sequenceTargetingReagentType} alias:</form:label>
        <form:input onkeypress="return noenter(event)" path="<%= SequenceTargetingReagentAddBean.NEW_STR_ALLIAS%>" size="50"></form:input>
        <form:errors path="<%= SequenceTargetingReagentAddBean.NEW_STR_ALLIAS%>" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
        <form:label path="<%= SequenceTargetingReagentAddBean.NEW_STR_COMMENT%>" class="curation-form-label">Note:</form:label>
    </div>
    <div>
        <form:textarea path="<%= SequenceTargetingReagentAddBean.NEW_STR_COMMENT%>" rows="5" cols="50" />
        <form:errors path="<%= SequenceTargetingReagentAddBean.NEW_STR_COMMENT%>" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
        <b>Add Target Gene:</b><br/>
        <form:input path="<%= SequenceTargetingReagentAddBean.NEW_STR_TARGET%>" id="targetGeneSymbol" type="text" size="25" />
        <form:errors path="<%= SequenceTargetingReagentAddBean.NEW_STR_TARGET%>" cssClass="error indented-error"/>
    </div>
    <c:if test="${formBean.sequenceTargetingReagentType eq 'TALEN' || formBean.sequenceTargetingReagentType eq 'CRISPR'}">
        <br/>
        <div>
            <b>Add Supplier:</b><br/>

            <form:input path="<%= SequenceTargetingReagentAddBean.NEW_STR_SUPPLIER%>" id="supplierName" type="text" size="35" />
            <form:errors path="<%= SequenceTargetingReagentAddBean.NEW_STR_SUPPLIER%>" cssClass="error indented-error"/>
        </div>
    </c:if>
    <p/>
    <table border=0>
        <tr>
            <td valign=top nowrap>
                <c:if test="${formBean.sequenceTargetingReagentType eq 'Morpholino'}">
                    <label class="namesearchLabel">Sequence:</label>
                </c:if>
                <c:if test="${formBean.sequenceTargetingReagentType eq 'CRISPR'}">
                    <label class="namesearchLabel">Target Sequence:</label>
                </c:if>
                <c:if test="${formBean.sequenceTargetingReagentType eq 'TALEN'}">
                    <label class="namesearchLabel">Target Sequence 1:</label>
                </c:if>
                <input type=button value="Reverse" onClick="sequenceManipulated='yes';reverseSequence('reportSeq','displaySeq');">
                <input type=button value="Complement" onClick="sequenceManipulated='yes';complementSequence('reportSeq','displaySeq');">
                <input type=button value="Reverse & Complement" onClick="sequenceManipulated='yes';reverseComplementSequence('reportSeq','displaySeq');">
            </td>
            <td width=20></td><!-- spacer column -->
            <td><form:label path="<%= SequenceTargetingReagentAddBean.NEW_STR_CURNOTE%>" class="curation-form-label">Curator Note:</form:label></td>
        </tr>
        <tr>
            <td nowrap>
                Reported: &nbsp;5'-   <form:input id="reportSeq" path="<%= SequenceTargetingReagentAddBean.NEW_STR_REPORTEDSEQUENCE%>" name="reportSeq" size="${seqBoxSize}" onChange="this.value = this.value.toUpperCase()" onkeypress="return noenter(event)"></form:input> -3'
            </td>
            <td width=20></td><!-- spacer column -->
            <c:choose>
                <c:when test="${formBean.sequenceTargetingReagentType eq 'TALEN'}">
                    <c:set var="curatorNoteRowspanValue">5</c:set>
                    <c:set var="curatorNoteRowValue">7</c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="curatorNoteRowspanValue">2</c:set>
                    <c:set var="curatorNoteRowValue">5</c:set>
                </c:otherwise>
            </c:choose>
            <td valign=top rowspan=${curatorNoteRowspanValue}>
                <form:textarea id="curatorNote" path="<%= SequenceTargetingReagentAddBean.NEW_STR_CURNOTE%>" rows="${curatorNoteRowValue}" cols="70" />
                <form:errors path="<%= SequenceTargetingReagentAddBean.NEW_STR_CURNOTE%>" cssClass="error indented-error"/>
            </td>
        </tr>
        <tr>
            <td nowrap>
                Displayed: 5'- <form:input id="displaySeq" name="displaySeq" path="<%= SequenceTargetingReagentAddBean.NEW_STR_SEQUENCE%>" size="${seqBoxSize}" onChange="this.value = this.value.toUpperCase()" onkeypress="return noenter(event)"></form:input> -3'
                <form:errors path="<%= SequenceTargetingReagentAddBean.NEW_STR_SEQUENCE%>" cssClass="error indented-error"/>
            </td>
        </tr>
        <c:if test="${formBean.sequenceTargetingReagentType eq 'TALEN'}">
            <tr>
                <td nowrap>
                    <label class="namesearchLabel">Target Sequence 2:</label>
                    <input type=button value="Reverse" onClick="sequence2Manipulated='yes';reverseSequence('reportSeq2','displaySeq2');">
                    <input type=button value="Complement" onClick="sequence2Manipulated='yes';complementSequence('reportSeq2','displaySeq2');">
                    <input type=button value="Reverse & Complement" onClick="sequence2Manipulated='yes';reverseComplementSequence('reportSeq2','displaySeq2');">
                </td>
                <td width=20></td><!-- spacer column -->
            </tr>
            <tr>
                <td nowrap>
                    Reported: &nbsp;5'-   <form:input id="reportSeq2" path="<%= SequenceTargetingReagentAddBean.NEW_STR_SECOND_REPORTEDSEQUENCE%>" name="reportSeq2" size="${seqBoxSize}" onChange="this.value = this.value.toUpperCase()" onkeypress="return noenter(event)"></form:input> -3'
                </td>
                <td width=20></td><!-- spacer column -->
            </tr>
            <tr>
                <td nowrap>
                    Displayed:&nbsp;&nbsp;  5'- <form:input id="displaySeq2" name="displaySeq2" path="<%= SequenceTargetingReagentAddBean.NEW_STR_SECOND_SEQUENCE%>" size="${seqBoxSize}" onChange="this.value = this.value.toUpperCase()" onkeypress="return noenter(event)"></form:input> -3'
                    <form:errors path="<%= SequenceTargetingReagentAddBean.NEW_STR_SECOND_SEQUENCE%>" cssClass="error indented-error"/>                </td>
            </tr>
        </c:if>
    </table>
    <p/>
    <div>
        <form:label path="<%= SequenceTargetingReagentAddBean.STR_PUBLICATION_ZDB_ID%>" class="curation-form-label">Publication:</form:label>
        <form:input path="<%= SequenceTargetingReagentAddBean.STR_PUBLICATION_ZDB_ID%>" size="25" onChange="this.value = this.value.toUpperCase()"
                    onkeypress="return noenter(event)" value="${formBean.sequenceTargetingReagentPublicationID}" id = "publicationZdbId"></form:input>
        <form:errors path="<%= SequenceTargetingReagentAddBean.STR_PUBLICATION_ZDB_ID%>" cssClass="error indented-error"/>
    </div>
    <p/>
    <c:if test="${formBean.sequenceTargetingReagentType eq 'TALEN'}">
        <input type=submit name=s_new value="Submit new ${formBean.sequenceTargetingReagentType}" onclick="preSubmit('talen')">
    </c:if>
    <c:if test="${formBean.sequenceTargetingReagentType ne 'TALEN'}">
        <input type=submit name=s_new value="Submit new ${formBean.sequenceTargetingReagentType}" onclick="preSubmit('nonTalen')">
    </c:if>
</form:form>

</html>

<script type="text/javascript">
    var sequenceManipulated = "no";
    var sequence2Manipulated = "no";

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

    function reverseSequence(reported, displayed) {
        var displayedSeq = document.getElementById(displayed);
        var reportSeq = document.getElementById(reported);
        var revSeq = reverseString(reportSeq.value);

        displayedSeq.value = revSeq;

        doCuratorNote('reversed',reported);
    }

    function complementSequence(reported, displayed) {
        var displayedSeq = document.getElementById(displayed);
        var reportSeq = document.getElementById(reported);
        var compSeq = baseComplement(reportSeq.value);

        displayedSeq.value = compSeq;

        doCuratorNote('complemented',reported);
    }


    function reverseComplementSequence(reported, displayed) {
        var displayedSeq = document.getElementById(displayed);
        var reportSeq = document.getElementById(reported);
        var revSeq = reverseString(reportSeq.value);
        var compSeq = baseComplement(revSeq);

        displayedSeq.value = compSeq;

        doCuratorNote('reversed and complemented',reported);
    }

    function doCuratorNote(action,reported) {
        var cNote = document.getElementById('curatorNote');
        var reportSeq = document.getElementById(reported);
        cNote.value = "Reported Sequence: "+ reportSeq.value + " was " + action +".\r\n" + cNote.value;
    }

    function preSubmit(type) {

        var displayedSeq = document.getElementById('displaySeq');
        var reportSeq = document.getElementById('reportSeq');
        if(!displayedSeq.value || displayedSeq.value.length == 0 || !/^[\s]+$/.test(displayedSeq.value)) {
            if (sequenceManipulated == "no") {
                displayedSeq.value = reportSeq.value;
            }
        }
        if (type === 'talen') {
            var displayedSeq2 = document.getElementById('displaySeq2');
            var reportSeq2 = document.getElementById('reportSeq2');

            if(!displayedSeq2.value || displayedSeq2.value.length == 0 || !/^[\s]+$/.test(displayedSeq2.value)) {
                if (sequence2Manipulated == "no") {

                    displayedSeq2.value = reportSeq2.value;

                }
            }
        }

        completePubId();
    }

    function completePubId() {

        var pubId = document.getElementById('publicationZdbId');
        var shortId = /^(\d){6}-(\d)+/;
        var shortIdStartWithHyphen = /^-(\d){6}-(\d)+/;
        if (shortId.test(pubId.value)) {
            pubId.value = "ZDB-PUB-" + pubId.value;
        }
        if (shortIdStartWithHyphen.test(pubId.value)) {
            pubId.value = "ZDB-PUB" + pubId.value;
        }
    }

    jQuery(document).ready(function () {

        $('#supplierName').autocompletify('/action/marker/find-suppliers?term=%QUERY');
        $('#targetGeneSymbol').autocompletify('/action/marker/find-targetGenes?term=%QUERY');

        $( "#supplierName" ).keypress(function(event) {
            if (event.keyCode == 13) {
                event.preventDefault();
                return false;
            }
        });


        $( "#targetGeneSymbol" ).keypress(function(event) {
            if (event.keyCode == 13) {
                event.preventDefault();
                return false;
            }
        });
    });

</script>


