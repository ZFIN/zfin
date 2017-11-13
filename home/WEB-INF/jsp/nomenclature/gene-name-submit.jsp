<jsp:useBean id="submission" scope="request" type="org.zfin.nomenclature.GeneNameSubmission"/>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<h3>Proposed Nomenclature</h3>
<dl>
    <dt>Gene Symbol</dt>
    <dd><c:out value="${submission.geneSymbol}" /></dd>
    <dt>Gene Name</dt>
    <dd><c:out value="${submission.geneName}" /></dd>
    <c:if test="${!empty submission.otherNames}">
        <dt>Other Names</dt>
        <dd><c:out value="${submission.otherNames}" /></dd>
    </c:if>
</dl>

<h3>Supporting Information</h3>
<dl>
    <c:if test="${!empty submission.genBankID}">
        <dt>GenBank ID</dt>
        <dd><c:out value="${submission.genBankID}" /></dd>
    </c:if>
    <c:if test="${!empty submission.sequence}">
        <dt>Sequence</dt>
        <dd><c:out value="${submission.sequence}" /></dd>
    </c:if>
    <c:if test="${!empty submission.chromosome}">
        <dt>Chromosome</dt>
        <dd><c:out value="${submission.chromosome}" /></dd>
    </c:if>
</dl>

<c:if test="${!empty submission.homologyInfoList}">
    <h4>Homology</h4>
    <c:forEach var="homologue" items="${submission.homologyInfoList}">
        <dl>
            <dt>Species</dt>
            <dd><c:out value="${homologue.species}" /></dd>
            <dt>Gene Symbol</dt>
            <dd><c:out value="${homologue.geneSymbol}" /></dd>
            <dt>Database ID</dt>
            <dd><c:out value="${homologue.databaseID}" /></dd>
        </dl>
    </c:forEach>
</c:if>
