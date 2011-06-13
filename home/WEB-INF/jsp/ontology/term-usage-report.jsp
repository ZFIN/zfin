<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

Back to <a href="/action/dev-tools/home"> Dev-tools</a>

<p></p>

<span class="summaryTitle">Term Phenotype Histogram</span><br/>
${fn:length(phenotypeUsage)} distinct terms used
<table class="searchresults">
    <tr style="background: #ccc">
        <th>ID</th>
        <th>Term</th>
        <th>Usage</th>
    </tr>
    <c:forEach var="report" items="${phenotypeUsage}" varStatus="loop">
        <tr>
            <td>${loop.index+1}</td>
            <td>${report.key.termName}</td>
            <td>${report.value}</td>
        </tr>
    </c:forEach>
</table>
