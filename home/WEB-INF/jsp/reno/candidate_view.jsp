<%@ page import="org.zfin.sequence.reno.presentation.CandidateBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<form:form name="candidateform" commandName="formBean" method="post"
           action="/action/reno/candidate-view?runCandidate.zdbID=${formBean.runCandidate.zdbID}"
           cssStyle="display: inline;">

Run name:
<zfin:link entity="${formBean.runCandidate.run}"/>
<small> [${formBean.runCandidate.run.type}]</small>
<br>
Candidate:
<c:if test="${formBean.runCandidate.run.nomenclature}">
    <zfin:link entity="${formBean.runCandidate}"/>
</c:if>
<c:if test="${formBean.runCandidate.run.redundancy}">
    <zfin:name entity="${formBean.runCandidate}"/>
</c:if>

    <input type="hidden" name="action">
    <!-- lock & unlock buttons -->
    &nbsp;                     ${org.springframework.validation.BindingResult.formBean}
    <c:choose>
        <c:when test="${formBean.runCandidate.locked}">

            <c:if test="${formBean.ownerViewing}">
                <%-- todo: set action=unlock--%>

                <input type="submit" value="Unlock"
                        onclick="document.forms.candidateform.action.value = '<%= CandidateBean.UNLOCK_RECORD %>';">
            </c:if>
            <c:if test="${!formBean.ownerViewing}">
                <!-- todo: replace with link tag for person, once link tag supports person -->
                <span style="color: red;">Locked by</span>: ${formBean.runCandidate.lockPerson.name}
                <form:errors path="action" cssClass="Error"/>
            </c:if>

        </c:when>
        <c:otherwise>
            <%--todo: set action=lock--%>
            <input type="submit" value="Lock and Edit"
                    onclick="document.forms.candidateform.action.value = '<%= CandidateBean.LOCK_RECORD %>';">
        </c:otherwise>
    </c:choose>

    <%-- problem / not problem buttons --%>
    <c:if test="${formBean.runCandidate.locked && formBean.runCandidate.run.redundancy}">
        <form:checkbox path="candidateProblem"
                       onchange="document.forms.candidateform.action.value='SET_PROBLEM';
                                 document.forms.candidateform.submit();"/>
        <label for="candidateProblem1">is problem</label>
    </c:if>
    <br>
    <br>

    <table width="100%">
        <tr>

            <c:if test="${formBean.ownerViewing}">
                <td valign="top">
                    <c:choose>
                        <c:when test="${formBean.runCandidate.run.nomenclature}">
                            <tiles:insertTemplate template="/WEB-INF/jsp/reno/nomen_edit.jsp" flush="false"/>
                        </c:when>
                        <c:when test="${formBean.runCandidate.run.redundancy}">
                            <tiles:insertTemplate template="/WEB-INF/jsp/reno/redundancy_edit.jsp" flush="false"/>
                        </c:when>
                    </c:choose>
                </td>
            </c:if>
            <td valign="top" align="right">
                <form:textarea path="candidateNote" rows="5" cols="40"></form:textarea>
                <br>
                    <%--todo: set action=savenote--%>
                <input type="submit" value="Save note" style="margin-top: .3em;"
                        onclick="document.forms.candidateform.action.value = '<%= CandidateBean.SAVE_NOTE %>';">
            </td>

        </tr>
    </table>
</form:form>
<br>
<br>


<c:choose>
    <c:when test="${formBean.runCandidate.run.nomenclature}">
        <tiles:insertTemplate template="/WEB-INF/jsp/reno/nomen_hit_list.jsp" flush="false"/>
        <!-- include jsp for nomenclature table -->
    </c:when>
    <c:when test="${formBean.runCandidate.run.redundancy}">
        <tiles:insertTemplate template="/WEB-INF/jsp/reno/redundancy_hit_list.jsp" flush="false"/>
        <!-- include jsp for redundancy table -->
    </c:when>
</c:choose>
