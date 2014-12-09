<jsp:useBean id="submission" scope="request" type="org.zfin.nomenclature.LineNameSubmission"/>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<h3>Line Information</h3>
<dl>
    <dt>Genetic Background</dt>
    <dd>${submission.geneticBackground}</dd>
</dl>

<c:if test="${!empty submission.lineDetails}">
<table class="summary">
    <tr>
        <th>Gene or Construct Name</th>
        <th>Gene Symbol</th>
        <th>Allele/Line Designation</th>
        <th>Protocol</th>
        <th>Mutation Type</th>
    </tr>
    <c:forEach var="info" items="${submission.lineDetails}">
        <tr>
            <td>${info.geneName}</td>
            <td>${info.geneSymbol}</td>
            <td>${info.designation}</td>
            <td>${info.protocol}</td>
            <td>${info.mutationType}</td>
        </tr>
    </c:forEach>
</table>
</c:if>
