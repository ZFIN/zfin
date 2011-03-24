<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="phenotypeExperiments" scope="request" type="java.util.List"/>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Phenotype Curation Summary</span> (5 days)</th>
    </tr>
    <tr>
        <td><a href="/action/dev-tools/phenotype-curation-history-statements">Show Statement History</a></td>
    </tr>
</table>

<table class="summary">
    <tr>
        <th>ID</th>
        <th>Publication</th>
        <th>Figure</th>
        <th>Genotype</th>
        <th>Experiment</th>
        <th colspan="2">Date Created</th>
    </tr>
    <c:forEach var="phenotypeExperiment" items="${phenotypeExperiments}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>
            <a href="/action/dev-tools/phenotype-curation-history-statements?experimentID=${phenotypeExperiment.id}">${phenotypeExperiment.id}</a></td>
            <td>
                <zfin:link entity="${phenotypeExperiment.figure.publication}"/>
                <a href="/<%=ZfinPropertiesEnum.MUTANT_NAME%>/webdriver?MIval=aa-curation.apg&OID=${phenotypeExperiment.figure.publication.zdbID}&cookie=tabPHENO">(Pheno</a>)
            </td>
            <td><zfin:link entity="${phenotypeExperiment.figure}"/></td>
            <td><zfin:link entity="${phenotypeExperiment.genotypeExperiment.genotype}"/></td>
            <td>${phenotypeExperiment.genotypeExperiment.experiment.name}</td>
            <td><fmt:formatDate value="${phenotypeExperiment.dateCreated}" /></td>
            <td><fmt:formatDate value="${phenotypeExperiment.dateCreated}" pattern="HH:mm:ss"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>





