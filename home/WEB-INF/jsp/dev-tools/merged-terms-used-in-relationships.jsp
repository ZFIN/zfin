<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage>
    <a href="/action/devtool/home"> Dev-tools</a>
    >
    <a href="/action/ontology/reports"> Ontology Reports</a>

    <p></p>
    <table class="primary-entity-attributes">
        <tr>
            <th><span class="name-label">${mergeTerms.size()} Merged Terms used in Term Relationships:</span>
            </th>
        </tr>
    </table>

    <p/>

    <table class="summary sortable">
        <tr>
            <th>#</th>
            <th>Term Name</th>
            <th>Term ID</th>
            <th>Ontology</th>
        </tr>
        <c:forEach var="term" items="${mergeTerms}" varStatus="index">
            <tr>
                <td>${index.index +1}</td>
                <td><zfin:link entity="${term}"/></td>
                <td>${term.oboID}</td>
                <td>${term.ontology}</td>
            </tr>
        </c:forEach>
    </table>
</z:devtoolsPage>