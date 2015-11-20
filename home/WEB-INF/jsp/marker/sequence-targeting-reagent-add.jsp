<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentAddBean" scope="request"/>

<h1>New ${formBean.strType}</h1>

<c:choose>
    <c:when test="${formBean.strType eq 'Morpholino'}">
        <c:set var="seqBoxSize">50</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="seqBoxSize">60</c:set>
    </c:otherwise>
</c:choose>


<form:form id="str-form" action="sequence-targeting-reagent-add?strType=${formBean.strType}" commandName="formBean" method="post">
    <div>
        <form:label path="name" class="curation-form-label">${formBean.strType} name:</form:label>
        <form:input path="name" size="80" />
        <form:errors path="name" cssClass="error"/>
    </div>
    <br>
    <div>
        <form:label path="alias" class="curation-form-label">${formBean.strType} alias:</form:label>
        <form:input path="alias" size="50"/>
        <form:errors path="alias" cssClass="error"/>
    </div>
    <br>
    <div>
        <form:label path="publicNote" class="curation-form-label">Note:</form:label>
    </div>
    <div>
        <form:textarea path="publicNote" rows="5" cols="50" />
        <form:errors path="publicNote" cssClass="error"/>
    </div>
    <br>
    <div>
        <b>Add Target Gene:</b><br/>
        <form:input path="targetGeneSymbol" type="text" size="25"/>
        <form:errors path="targetGeneSymbol" cssClass="error"/>
    </div>
    <c:if test="${formBean.strType eq 'TALEN' || formBean.strType eq 'CRISPR'}">
        <br/>
        <div>
            <b>Add Supplier:</b><br/>
            <form:input path="supplier" type="text" size="35" />
            <form:errors path="supplier" cssClass="error"/>
        </div>
    </c:if>
    <br>
    <table border=0>
        <tr>
            <td valign=top nowrap>
                <c:if test="${formBean.strType eq 'Morpholino'}">
                    <label class="namesearchLabel">Sequence:</label>
                </c:if>
                <c:if test="${formBean.strType eq 'CRISPR'}">
                    <label class="namesearchLabel">Target Sequence:</label>
                </c:if>
                <c:if test="${formBean.strType eq 'TALEN'}">
                    <label class="namesearchLabel">Target Sequence 1:</label>
                </c:if>
                <input type=button value="Reverse" id="reverse-1">
                <input type=button value="Complement" id="complement-1">
                <input type=button value="Reverse & Complement" id="reverse-complement-1">
            </td>
            <td width=20></td><!-- spacer column -->
            <td><form:label path="curatorNote" class="curation-form-label">Curator Note:</form:label></td>
        </tr>
        <tr>
            <td nowrap>
                Reported: &nbsp;5'-   <form:input path="reportedSequence" size="${seqBoxSize}" cssClass="force-upper" /> -3'
            </td>
            <td width=20></td><!-- spacer column -->
            <c:choose>
                <c:when test="${formBean.strType eq 'TALEN'}">
                    <c:set var="curatorNoteRowspanValue">5</c:set>
                    <c:set var="curatorNoteRowValue">7</c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="curatorNoteRowspanValue">2</c:set>
                    <c:set var="curatorNoteRowValue">5</c:set>
                </c:otherwise>
            </c:choose>
            <td valign=top rowspan=${curatorNoteRowspanValue}>
                <form:textarea path="curatorNote" rows="${curatorNoteRowValue}" cols="70" />
                <form:errors path="curatorNote" cssClass="error"/>
            </td>
        </tr>
        <tr>
            <td nowrap>
                Displayed: 5'- <form:input path="sequence" size="${seqBoxSize}" cssClass="force-upper" /> -3'
                <form:errors path="sequence" cssClass="error"/>
            </td>
        </tr>
        <c:if test="${formBean.strType eq 'TALEN'}">
            <tr>
                <td nowrap>
                    <label class="namesearchLabel">Target Sequence 2:</label>
                    <input type=button value="Reverse" id="reverse-2">
                    <input type=button value="Complement" id="complement-2">
                    <input type=button value="Reverse & Complement" id="reverse-complement-2">
                </td>
                <td width=20></td><!-- spacer column -->
            </tr>
            <tr>
                <td nowrap>
                    Reported: &nbsp;5'-   <form:input path="reportedSequence2" size="${seqBoxSize}" cssClass="force-upper" /> -3'
                </td>
                <td width=20></td><!-- spacer column -->
            </tr>
            <tr>
                <td nowrap>
                    Displayed:&nbsp;&nbsp;  5'- <form:input path="sequence2" size="${seqBoxSize}" cssClass="force-upper" /> -3'
                    <form:errors path="sequence2" cssClass="error"/>
                </td>
            </tr>
        </c:if>
    </table>
    <br>
    <div>
        <form:label path="publicationID" class="curation-form-label">Publication:</form:label>
        <form:input path="publicationID" size="25" cssClass="force-upper" value="${formBean.publicationID}"/>
        <form:errors path="publicationID" cssClass="error"/>
    </div>
    <br>
    <c:if test="${formBean.strType eq 'TALEN'}">
        <input type=submit name=s_new value="Submit new ${formBean.strType}" id="talen-submit">
    </c:if>
    <c:if test="${formBean.strType ne 'TALEN'}">
        <input type=submit name=s_new value="Submit new ${formBean.strType}" id="other-submit">
    </c:if>
</form:form>

<script type="text/javascript">
    $(function () {

        var sequenceManipulated = "no";
        var sequence2Manipulated = "no";

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
            displayedSeq.value = reverseString(reportSeq.value);

            doCuratorNote('reversed',reported);
        }

        function complementSequence(reported, displayed) {
            var displayedSeq = document.getElementById(displayed);
            var reportSeq = document.getElementById(reported);
            displayedSeq.value = baseComplement(reportSeq.value);

            doCuratorNote('complemented',reported);
        }


        function reverseComplementSequence(reported, displayed) {
            var displayedSeq = document.getElementById(displayed);
            var reportSeq = document.getElementById(reported);
            var revSeq = reverseString(reportSeq.value);
            displayedSeq.value = baseComplement(revSeq);

            doCuratorNote('reversed and complemented',reported);
        }

        function doCuratorNote(action,reported) {
            var cNote = document.getElementById('curatorNote');
            var reportSeq = document.getElementById(reported);
            cNote.value = "Reported Sequence: "+ reportSeq.value + " was " + action +".\r\n" + cNote.value;
        }

        function preSubmit(type) {

            var displayedSeq = document.getElementById('sequence');
            var reportSeq = document.getElementById('reportedSequence');
            if(!displayedSeq.value || displayedSeq.value.length == 0 || !/^[\s]+$/.test(displayedSeq.value)) {
                if (sequenceManipulated == "no") {
                    displayedSeq.value = reportSeq.value;
                }
            }
            if (type === 'talen') {
                var displayedSeq2 = document.getElementById('sequence2');
                var reportSeq2 = document.getElementById('reportedSequence2');

                if(!displayedSeq2.value || displayedSeq2.value.length == 0 || !/^[\s]+$/.test(displayedSeq2.value)) {
                    if (sequence2Manipulated == "no") {
                        displayedSeq2.value = reportSeq2.value;
                    }
                }
            }

            completePubId();
        }

        function completePubId() {

            var pubId = document.getElementById('publicationID');
            var shortId = /^(\d){6}-(\d)+/;
            var shortIdStartWithHyphen = /^-(\d){6}-(\d)+/;
            if (shortId.test(pubId.value)) {
                pubId.value = "ZDB-PUB-" + pubId.value;
            }
            if (shortIdStartWithHyphen.test(pubId.value)) {
                pubId.value = "ZDB-PUB" + pubId.value;
            }
        }

        $('#str-form').find('input').keypress(function(event) {
            if (event.which == 13) {
                event.preventDefault();
                return false;
            }
        });

        $('.force-upper').on('input', function() {
            $(this).val(function (index, value) {
                return value.toUpperCase();
            });
        });

        $("#reverse-1").click(function () {
            sequenceManipulated = 'yes';
            reverseSequence('reportedSequence','sequence');
        });
        $("#complement-1").click(function () {
            sequenceManipulated = 'yes';
            complementSequence('reportedSequence','sequence');
        });
        $("#reverse-complement-1").click(function () {
            sequenceManipulated = 'yes';
            reverseComplementSequence('reportedSequence','sequence');
        });
        $("#reverse-2").click(function () {
            sequence2Manipulated = 'yes';
            reverseSequence('reportedSequence2','sequence2');
        });
        $("#complement-2").click(function () {
            sequence2Manipulated = 'yes';
            complementSequence('reportedSequence2','sequence2');
        });
        $("#reverse-complement-2").click(function () {
            sequence2Manipulated = 'yes';
            reverseComplementSequence('reportedSequence2','sequence2');
        });

        $("#talen-submit").click(function() { preSubmit('talen'); });
        $("#other-submit").click(function() { preSubmit('nonTalen'); });

        $('#supplier').autocompletify('/action/marker/find-suppliers?term=%QUERY');
        $('#targetGeneSymbol').autocompletify('/action/marker/find-targetGenes?term=%QUERY');
    });

</script>


