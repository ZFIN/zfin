<%@ page import="org.zfin.orthology.OrthoEvidence" %>
<%@ page import="org.zfin.sequence.reno.presentation.CandidateBean" %>
<%@ page import="org.zfin.sequence.reno.presentation.RunBean" %>
<!-- called by candidate_view.jsp -->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Ajax includes --%>

<script src="/javascript/ajax-lib/prototype.js" type="text/javascript"></script>
<script src="/javascript/ajax-lib/effects.js" type="text/javascript"></script>
<script src="/javascript/ajax-lib/dragdrop.js" type="text/javascript"></script>
<script src="/javascript/ajax-lib/controls.js" type="text/javascript"></script>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<style type="text/css">
    .indented-label {
        float: left;
        width: 11em;
        text-align: right;
    }

    .indented-error {
        margin-left: 11em;
    }

</style>

<zfin2:errors errorResult="${errors}" />

<div style="border: 1px solid #ccc; background: #bbddf6; width: 28em; padding: .25em;">
    <label for="geneName" class="indented-label">Gene Name:</label>
    <form:input path="<%= CandidateBean.NEW_GENE_NAME%>" size="25"></form:input>
    <form:errors path="<%= CandidateBean.NEW_GENE_NAME%>" cssClass="error indented-error"/>
    <br>
    <label for="geneAbbreviation" class="indented-label">Candidate Gene:</label>
    <form:input path="<%= CandidateBean.NEW_ABBREVIATION%>" size="25"></form:input>
    <br>
    <form:errors path="<%= CandidateBean.NEW_ABBREVIATION%>" cssClass="error indented-error"/>
    <br>

    <label for="humanOrthologueAbbrev" class="indented-label">Human:</label>

    <form:select path="humanOrthologueAbbrev.entrezAccession.entrezAccNum">
        <option value="">-</option>
        <form:options items="${formBean.runCandidate.humanOrthologuesFromQueries}"
                      itemLabel="entrezAccession.abbreviation" itemValue="entrezAccession.entrezAccNum"/>
    </form:select>


    <%-- numbering is how spring translates, if we can get the form:label tag to work,
that would be better --%>
    <label for="humanOrthologyEvidence1"><%= OrthoEvidence.Code.AA.name()%>
    </label>
    <form:checkbox path="<%= CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE %>"
                   value="<%= OrthoEvidence.Code.AA.name()%>"></form:checkbox>
    <label for="humanOrthologyEvidence2"><%= OrthoEvidence.Code.CL.name()%>
    </label>
    <form:checkbox path="<%= CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE %>"
                   value="<%= OrthoEvidence.Code.CL.name()%>"></form:checkbox>
    <label for="humanOrthologyEvidence3"><%= OrthoEvidence.Code.NT.name()%>
    </label>
    <form:checkbox path="<%= CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE %>"
                   value="<%= OrthoEvidence.Code.NT.name()%>"></form:checkbox>
    <form:errors path="<%= CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE %>" cssClass="error indented-error"/>
    <form:errors path="humanOrthologueAbbrev.entrezAccession.entrezAccNum" cssClass="error indented-error"/>


    <br>
    <label for="mouseOrthologueAbbrev" class="indented-label">Mouse:</label>
    <form:select path="mouseOrthologueAbbrev.entrezAccession.entrezAccNum">
        <option value="">-</option>
        <form:options items="${formBean.runCandidate.mouseOrthologuesFromQueries}"
                      itemLabel="entrezAccession.abbreviation" itemValue="entrezAccession.entrezAccNum"/>
    </form:select>


    <label for="mouseOrthologyEvidence1"><%= OrthoEvidence.Code.AA.name()%>
    </label>
    <form:checkbox path="<%= CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE %>"
                   value="<%= OrthoEvidence.Code.AA.name()%>"></form:checkbox>
    <label for="mouseOrthologyEvidence2"><%= OrthoEvidence.Code.CL.name()%>
    </label>
    <form:checkbox path="<%= CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE %>"
                   value="<%= OrthoEvidence.Code.CL.name()%>"></form:checkbox>
    <label for="mouseOrthologyEvidence3"><%= OrthoEvidence.Code.NT.name()%>
    </label>
    <form:checkbox path="<%= CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE %>"
                   value="<%= OrthoEvidence.Code.NT.name()%>"></form:checkbox>
    <form:errors path="<%= CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE %>" cssClass="error indented-error"/>
    <form:errors path="mouseOrthologueAbbrev.entrezAccession.entrezAccNum" cssClass="error indented-error"/>

    <br>
    <br>

    <label for="geneFamilyName" class="indented-label">In Family:</label>
    <form:input size="35" path="geneFamilyName" id="geneFamilyName"/>
    <div style="overflow: auto; height: 400px; width: 400px;" class="auto_complete"
         id="geneFamilyNameAutoComplete"></div>

    <script type="text/javascript">
        var geneFamilyNameAutoCompleter = new Ajax.Autocompleter('geneFamilyName', 'geneFamilyNameAutoComplete', '/action/autocomplete/gene-family', {paramName: "query", minChars: 1});
    </script>
    <form:errors path="geneFamilyName" cssClass="error indented-error"/>
    <br>
    <br>

    <label for="<%= RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID%>" class="indented-label">Nomen Pub:</label>
    <form:input path="<%= RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID%>" size="25"></form:input>
    <form:errors path="<%= RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID%>" cssClass="error indented-error"/>
    <br>
    <label for="<%= RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID%>" class="indented-label">Orthology Pub:</label>
    <form:input path="<%= RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID%>" size="25"></form:input>
    <form:errors path="<%= RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID%>" cssClass="error indented-error"/>
    <br><br>
    <%--todo: set action=done--%>
    <input value="done" type="submit" style="margin-left: 15em; margin-top: .5em; margin-bottom:.25em;"
           onclick=" document.forms.candidateform.action.value = '<%=CandidateBean.DONE%>'; ">
    <form:errors path="action" cssClass="error"/>

</div>