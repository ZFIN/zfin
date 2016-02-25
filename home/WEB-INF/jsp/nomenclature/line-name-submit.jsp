<jsp:useBean id="submission" scope="request" type="org.zfin.nomenclature.LineNameSubmission"/>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<h3>Line Information</h3>

<c:forEach var="info" items="${submission.lineDetails}">
    <dl>
        <dt>Genetic Background</dt>
        <dd>${info.background}</dd>
        <dt>Gene or Construct Name</dt>
        <dd>${info.geneName}</dd>
        <dt>Gene Symbol</dt>
        <dd>${info.geneSymbol}</dd>
        <dt>Allele/Line Designation</dt>
        <dd>${info.designation}</dd>
        <dt>Protocol</dt>
        <dd>${info.protocol}</dd>
        <dt>Mutation Type</dt>
        <dd>${info.mutationType}</dd>
        <dt>Mutation Details</dt>
        <dd>${info.mutationDetails}</dd>
        <dt>CRISPR or TALEN Sequence</dt>
        <dd>${info.sequence}</dd>
    </dl>
</c:forEach>
