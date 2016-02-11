<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="Phenotypic Expression Statement"/>
    </tiles:insertTemplate>
</div>
<table class="primary-entity-attributes">
    <tr>
        <th>Phenotype:</th>
        <td><zfin:name entity="${phenotypeStatement}"/></td>
    </tr>
    <tr>
        <th>Note:</th>
        <td>
            This phenotype statement describes a gene expression or antibody
            labeling pattern. Anatomy terms are combined with a gene or antibody
            and phenotype quality.
        </td>
    </tr>
</table>

<zfin2:subsection title="EXPRESSION ANATOMY:">
    <zfin2:termMiniSummary term="${phenotypeStatement.e1a}"/>
    <c:if test="${not empty phenotypeStatement.e1b}">
        <zfin2:termMiniSummary term="${phenotypeStatement.e1b}"/>
    </c:if>
</zfin2:subsection>

<zfin2:subsection title="${phenotypeStatement.gene.type == 'GENE' ? 'EXPRESSED GENE:' : 'ANTIBODY LABELING:'}">
    <zfin2:markerSummary marker="${phenotypeStatement.gene}" previousNames="${genePreviousNames}"/>
</zfin2:subsection>

<zfin2:subsection title="EXPRESSION QUALITY:">
    <zfin2:termMiniSummary term="${phenotypeStatement.quality}"/>
</zfin2:subsection>