<%@ page import="org.zfin.sequence.reno.presentation.CandidateBean" %>
<!-- called by candidate_view.jsp -->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<style type="text/css">
    .indented-label {
        float: left;
        width: 7em;
        text-align: right;
    }

    .indented-error {
        padding-left: 7em;
    }
</style>

<div id="redundancy-edit" style="border: 1px solid #ccc; background: lightyellow; width: 20em; padding:.25em;">

    <div>
        <form:label path="associatedGeneField" cssClass="indented-label">Associate with:</form:label>
        <form:select path="associatedGeneField">
            <form:option value="IGNORE">IGNORE</form:option>
            <form:option value="<%=CandidateBean.NOVEL%>"
                         label="${formBean.runCandidate.candidate.suggestedName} (novel)"></form:option>
            <c:if test="${!formBean.alreadyAssociatedGenes}">
                <form:options items="${formBean.allSingleAssociatedGenesFromQueries}"
                              itemLabel="abbreviation" itemValue="zdbID"/>
            </c:if>
        </form:select>
        <form:errors path="associatedGeneField" cssClass="error indented-error"/>
    </div>

    <div>
        <form:label path="geneZdbID" cssClass="indented-label">or ZDB: </form:label>
        <form:input path="geneZdbID" size="25"></form:input>
        <form:errors path="geneZdbID" cssClass="error indented-error"/>

    </div>

    <div>
        <span class="indented-label">
        <form:checkbox path="rename"></form:checkbox>
        <label for="rename1">Rename: </label>
        </span>

        <form:input path="geneAbbreviation" size="25"></form:input>
        <form:errors path="geneAbbreviation" cssClass="error indented-error"/>
    </div>


    <input type="submit" value="done" style="margin-left: 12em; margin-top:.5em; margin-bottom:.25em;"
           onclick="document.forms.candidateform.action.value = 'done';">
    <form:errors path="action" cssClass="error"/>

</div>

<div style="color:red">
    ${formBean.message}
</div>
