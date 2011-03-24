<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="phenotypeStatements" scope="request" type="java.util.List"/>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Phenotype Curation Summary: Statements</span> (last 2 days)</th>
    </tr>
    <tr>
        <td><a href="/action/dev-tools/phenotype-curation-history">Show Experiment History</a></td>
    </tr>
</table>

<table class="summary">
    <tr>
        <th>ID</th>
        <th>Exp ID</th>
        <th>Publication</th>
        <th>Figure</th>
        <th>Genotype</th>
        <th>Experiment</th>
        <th>Statement</th>
        <th colspan="2">Date Created</th>
    </tr>
    <c:forEach var="phenotypeStatement" items="${phenotypeStatements}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>${phenotypeStatement.id}</td>
            <td><a href="">${phenotypeStatement.phenotypeExperiment.id}</a></td>
            <td>
                <zfin:link entity="${phenotypeStatement.phenotypeExperiment.figure.publication}"/>
                <a href="/<%=ZfinPropertiesEnum.MUTANT_NAME%>/webdriver?MIval=aa-curation.apg&OID=${phenotypeStatement.phenotypeExperiment.figure.publication.zdbID}&cookie=tabPHENO">(Pheno</a>)
            <td><zfin:link entity="${phenotypeStatement.phenotypeExperiment.figure}" /></td>
            <td><zfin:link entity="${phenotypeStatement.phenotypeExperiment.genotypeExperiment.genotype}"/></td>
            <td><zfin:link entity="${phenotypeStatement.phenotypeExperiment.genotypeExperiment.experiment}"/></td>
            <td><zfin:link entity="${phenotypeStatement}"/></td>
            <td><zfin2:displayDay date="${phenotypeStatement.dateCreated}"/></td>
            <td><fmt:formatDate value="${phenotypeStatement.dateCreated}" pattern="HH:mm:ss"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>





