<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<h3>Proposed Nomenclature</h3>

<div>
    <form:label path="geneSymbol" cssClass="required">Gene Symbol</form:label>
    <form:input path="geneSymbol"/>
    <em>e.g. ndr2</em>
</div>

<div>
    <form:label path="geneName" cssClass="required">Gene Name</form:label>
    <form:input path="geneName"/>
    <em>e.g. nodal-related 2</em>
</div>

<div>
    <form:label path="otherNames">Other Names</form:label>
    <form:input path="otherNames"/>
</div>

<h3>Supporting Information</h3>

<p>
    An appropriate gene name and symbol may depend on various pieces of evidence, such as orthology to a mammalian
    gene that has an established name, or whether it appears to be a duplicate resulting from whole-genome
    duplication, based on high sequence identity and conserved synteny.
</p>
<p>
    To expedite the review process for your proposed name, please provide the following information, if possible.
    If it is more convenient to provide data in a separate file, please
    <a href="mailto:<%= ZfinPropertiesEnum.NOMEN_COORDINATOR.value()%>">email it directly to us</a>.
</p>
<p>
    <em>Sequence data will be treated in complete confidence.</em>
</p>

<div>
    <form:label path="genBankID">GenBank ID</form:label>
    <form:input path="genBankID"/>
</div>

<div>
    <form:label path="sequence">Sequence</form:label>
    <form:textarea path="sequence" cols="80" rows="5"/>
</div>

<div>
    <form:label path="chromosome">Chromosome</form:label>
    <form:input path="chromosome"/>
</div>

<h4>Homology</h4>

<table id="homologues">
    <tr>
        <th>Species</th>
        <th>Gene Symbol</th>
        <th>Database ID <small>(e.g.,
            <a href="http://www.ncbi.nlm.nih.gov/Genbank/index.html" target="_blank">GenBank</a>,
            <a href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=OMIM" target="_blank">OMIM</a>,
            <a href="http://www.informatics.jax.org/" target="_blank">Mouse Genome Informatics</a>)</small></th>
    </tr>
    <c:forEach var="homologue" items="${submission.homologyInfoList}" varStatus="status">
        <tr>
            <td><form:input path="homologyInfoList[${status.index}].species"/></td>
            <td><form:input path="homologyInfoList[${status.index}].geneSymbol"/></td>
            <td><form:input path="homologyInfoList[${status.index}].databaseID"/></td>
        </tr>
    </c:forEach>
</table>

<script>
    jQuery(function () {
        makeDynamicTable("#homologues", ${fn:length(submission.homologyInfoList)}, "homologyInfoList",
                ["species", "geneSymbol", "databaseID"]);
    });
</script>