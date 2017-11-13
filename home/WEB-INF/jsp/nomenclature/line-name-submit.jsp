<jsp:useBean id="submission" scope="request" type="org.zfin.nomenclature.LineNameSubmission"/>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<h3>Line Information</h3>

<c:forEach var="info" items="${submission.lineDetails}">
    <dl>
        <dt>Genetic Background</dt>
        <dd><c:out value="${info.background}" /></dd>
        <dt>Gene or Construct Name</dt>
        <dd><c:out value="${info.geneName}" /></dd>
        <dt>Gene Symbol</dt>
        <dd><c:out value="${info.geneSymbol}" /></dd>
        <dt>Allele/Line Designation</dt>
        <dd><c:out value="${info.designation}" /></dd>
        <dt>Protocol</dt>
        <dd><c:out value="${info.protocol}" /></dd>
        <dt>Mutation Type</dt>
        <dd><c:out value="${info.mutationType}" /></dd>
        <dt>Mutation Details</dt>
        <dd><c:out value="${info.mutationDetails}" /></dd>
        <dt>CRISPR or TALEN Sequence</dt>
        <dd><c:out value="${info.sequence}" /></dd>
    </dl>
</c:forEach>
