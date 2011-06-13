<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

Back to <a href="/action/dev-tools/home"> Dev-tools</a>
|
Back to <a href="/action/ontology/reports"> Ontology Reports</a>

<p></p>

<span class="summaryTitle">Secondary Terms used in Phenotype Annotations: ${numberOfPhenotypesOnSecondaryTerms}</span>
<table class="searchresults">
    <tr style="background: #ccc">
        <th>Edit</th>
        <th>Publication</th>
        <th>Figure</th>
        <th>Phenotype</th>
        <th>Merged (secondary)  Term</th>
        <th>Replace By Term</th>
        <th>Consider Term</th>
    </tr>
    <c:forEach var="report" items="${phenotypeSecondaryTermReports}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${phenotypeSecondaryTermReports}">
            <td><zfin:link entity="${report.phenotypeStatement}" curationLink="true"/></td>
            <td><zfin:link entity="${report.phenotypeStatement.phenotypeExperiment.figure.publication}"/></td>
            <td><zfin:link entity="${report.phenotypeStatement.phenotypeExperiment.figure}"/></td>
            <td><zfin:link entity="${report.phenotypeStatement}"/></td>
            <td>
                <c:forEach var="term" items="${report.obsoletedTermList}" >
                    <zfin:link entity="${term}"/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="replacedByTerm" items="${report.replacementTermList}" >
                    <zfin:link entity="${replacedByTerm.replacementTerm}"/><br/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="considerTerm" items="${report.considerTermList}" >
                    <zfin:link entity="${considerTerm.considerTerm}"/><br/>
                </c:forEach>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

<p/>

<span class="summaryTitle">Secondary Terms used in Expression Annotations: ${numberOfExpressionsOnSecondaryTerms}</span>
<table class="searchresults">
    <tr style="background: #ccc">
        <th>Publication</th>
        <th>Figure</th>
        <th>Expression</th>
        <th>Merged (secondary) Term</th>
        <th>Replace By Term</th>
        <th>Consider Term</th>
    </tr>
    <c:forEach var="report" items="${expressionObsoleteTermReports}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${expressionObsoleteTermReports}">
            <td><zfin:link entity="${report.expressionResult.expressionExperiment.publication}"/></td>
            <td><zfin:link entity="${report.expressionResult.expressionExperiment}"/></td>
            <td>
                <c:forEach var="figure" items="${report.expressionResult.figures}" >
                    <zfin:link entity="${figure}"/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="term" items="${report.obsoletedTermList}" >
                    <zfin:link entity="${term}"/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="replacedByTerm" items="${report.replacementTermList}" >
                    <zfin:link entity="${replacedByTerm.replacementTerm}"/><br/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="considerTerm" items="${report.considerTermList}" >
                    <zfin:link entity="${considerTerm.considerTerm}"/><br/>
                </c:forEach>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
<p/>

<span class="summaryTitle">Secondary Terms used in Go Evidences: ${numberOfGoEvidenceOnSecondaryTerms}</span>
<table class="searchresults">
    <tr style="background: #ccc">
        <th>Publication</th>
        <th>Gene</th>
        <th>Merged (secondary) Term</th>
        <th>Replace By Term</th>
        <th>Consider Term</th>
    </tr>
    <c:forEach var="report" items="${goEvidenceSecondaryTermReports}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${goEvidenceSecondaryTermReports}">
            <td><zfin:link entity="${report.goEvidence.source}"/></td>
            <td><zfin:link entity="${report.goEvidence.marker}"/></td>
            <td>
                <c:forEach var="term" items="${report.obsoletedTermList}" >
                    <zfin:link entity="${term}"/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="replacedByTerm" items="${report.replacementTermList}" >
                    <zfin:link entity="${replacedByTerm.replacementTerm}"/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="considerTerm" items="${report.considerTermList}" >
                    <zfin:link entity="${considerTerm.considerTerm}"/><br/>
                </c:forEach>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

<script language="javascript">
    function setCookie(name,value,prefix) {
      var thisCookie = prefix+name;
      document.cookie = thisCookie+"="+value;
      //alert (thisCookie+"="+value);
    }



    function urlSetCookie(name,value,prefix) {
      setCookie(name,value,prefix);
    	replaceLocation("<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT%>?MIval=aa-curation.apg&OID=$OID&cookie="+name+value+"&randomNum="+Math.random()+"#"+name);
    }

</script>
