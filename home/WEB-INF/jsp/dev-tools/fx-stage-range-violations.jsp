<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage>
    <a href="/action/devtool/home"> Dev-tools</a>
    >
    <a href="/action/ontology/reports"> Ontology Reports</a>

    <p></p>
    <table class="primary-entity-attributes">
        <tr>
            <th><span class="name-label">${expressionResultDisplays.size()} FX Stage Range Violations:</span> Term stage
                range does not overlap with expression stage range.
            </th>
        </tr>
    </table>

    <p/>

    <table class="summary sortable">
        <tr>
            <th>Term Name</th>
            <th>Term Start Stage</th>
            <th>Term End Stage</th>
            <th>Figure Start Stage</th>
            <th>Figure End Stage</th>
            <th>#</th>
            <th>Publication</th>
        </tr>
        <c:forEach var="display" items="${expressionResultDisplays}" varStatus="index">
            <tr>
                <td><zfin:link entity="${display.superterm}"/></td>
                <td>${display.superterm.start.name}</td>
                <td>${display.superterm.end.name}</td>
                <td>${display.start.name}</td>
                <td>${display.end.name}</td>
                <td>${display.expressionResultList.size()}</td>
                <td><zfin:link entity="${display.distinctPublications}"/></td>
            </tr>
        </c:forEach>
        <tfoot>
        <tr>
            <th colspan="5"> Total violations (Expression Results)</th>
            <th colspan="2">${violations.size()}</th>
        </tr>
        </tfoot>
    </table>
</z:devtoolsPage>
