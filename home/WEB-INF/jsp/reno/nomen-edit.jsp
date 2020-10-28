<%@ page import="org.zfin.sequence.reno.presentation.CandidateBean" %>
<%@ page import="org.zfin.sequence.reno.presentation.RunBean" %>
<%@ page import="org.zfin.orthology.EvidenceCodeCode" %>
<!-- called by candidate_view.jsp -->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

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
    <form:input path="${CandidateBean.NEW_GENE_NAME}" size="25"></form:input>
    <form:errors path="${CandidateBean.NEW_GENE_NAME}" cssClass="error indented-error"/>
    <br>
    <label for="geneAbbreviation" class="indented-label">Gene Symbol:</label>
    <form:input path="${CandidateBean.NEW_ABBREVIATION}" size="25"></form:input>
    <br>
    <form:errors path="${CandidateBean.NEW_ABBREVIATION}" cssClass="error indented-error"/>
    <br>

    <label for="humanOrthologAbbrev" class="indented-label">Human:</label>

    <form:select path="humanOrthologAbbrev.entrezAccession.entrezAccNum">
        <option value="">-</option>
        <form:options items="${formBean.runCandidate.humanOrthologsFromQueries}"
                      itemLabel="entrezAccession.abbreviation" itemValue="entrezAccession.entrezAccNum"/>
    </form:select>


    <%-- numbering is how spring translates, if we can get the form:label tag to work,
that would be better --%>
    <label for="humanOrthologyEvidence1">${EvidenceCodeCode.AA}
    </label>
    <form:checkbox path="${CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE}"
                   value="${EvidenceCodeCode.AA}"></form:checkbox>
    <label for="humanOrthologyEvidence2">${EvidenceCodeCode.CL}
    </label>
    <form:checkbox path="${CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE}"
                   value="${EvidenceCodeCode.CL}"></form:checkbox>
    <label for="humanOrthologyEvidence3">${EvidenceCodeCode.NT}
    </label>
    <form:checkbox path="${CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE}"
                   value="${EvidenceCodeCode.NT}"></form:checkbox>

    <label for="humanOrthologyEvidence4">${EvidenceCodeCode.PT}
    </label>
    <form:checkbox path="${CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE}"
                   value="${EvidenceCodeCode.PT}"></form:checkbox>
    <form:errors path="${CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE}" cssClass="error indented-error"/>


    <form:errors path="humanOrthologAbbrev.entrezAccession.entrezAccNum" cssClass="error indented-error"/>


    <br>
    <label for="mouseOrthologAbbrev" class="indented-label">Mouse:</label>
    <form:select path="mouseOrthologAbbrev.entrezAccession.entrezAccNum">
        <option value="">-</option>
        <form:options items="${formBean.runCandidate.mouseOrthologsFromQueries}"
                      itemLabel="entrezAccession.abbreviation" itemValue="entrezAccession.entrezAccNum"/>
    </form:select>


    <label for="mouseOrthologyEvidence1">${EvidenceCodeCode.AA}
    </label>
    <form:checkbox path="${CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE}"
                   value="${EvidenceCodeCode.AA}"></form:checkbox>
    <label for="mouseOrthologyEvidence2">${EvidenceCodeCode.CL}
    </label>
    <form:checkbox path="${CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE}"
                   value="${EvidenceCodeCode.CL}"></form:checkbox>
    <label for="mouseOrthologyEvidence3">${EvidenceCodeCode.NT}
    </label>
    <form:checkbox path="${CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE}"
                   value="${EvidenceCodeCode.NT}"></form:checkbox>
    <label for="mouseOrthologyEvidence4">${EvidenceCodeCode.PT}
    </label>
    <form:checkbox path="${CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE}"
                   value="${EvidenceCodeCode.PT}"></form:checkbox>

    <form:errors path="${CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE}" cssClass="error indented-error"/>
    <form:errors path="mouseOrthologAbbrev.entrezAccession.entrezAccNum" cssClass="error indented-error"/>

    <br>
    <br>

    <label for="geneFamilyName" class="indented-label">In Family:</label>
    <form:input size="35" path="geneFamilyName" id="geneFamilyName"/>

    <script>
        jQuery(document).ready(function() { jQuery('#geneFamilyName').autocompletify('/action/autocomplete/gene-family?query=%QUERY') });
    </script>

    <form:errors path="geneFamilyName" cssClass="error indented-error"/>
    <br>
    <br>

    <label for="${RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID}" class="indented-label">Nomen Pub:</label>
    <form:input path="${RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID}" size="25"></form:input>
    <form:errors path="${RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID}" cssClass="error indented-error"/>
    <br>
    <label for="${RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID}" class="indented-label">Orthology Pub:</label>
    <form:input path="${RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID}" size="25"></form:input>
    <form:errors path="${RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID}" cssClass="error indented-error"/>
    <br><br>
    <%--todo: set action=done--%>
    <input value="done" type="submit" style="margin-left: 15em; margin-top: .5em; margin-bottom:.25em;"
           onclick=" document.forms.candidateform.action.value = '${CandidateBean.DONE}'; ">
    <form:errors path="action" cssClass="error"/>

</div>