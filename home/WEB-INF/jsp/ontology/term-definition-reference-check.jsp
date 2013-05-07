<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
Back to <a href="/action/devtool/home"> Dev-tools</a>

<p></p>

<span class="summaryTitle">Invalid reference Hyperlinks</span>

<table class="searchresults">
    <tr>
        <th> Term name</th>
        <th> OBO ID</th>
        <th> Term Reference</th>
    </tr>
    <c:forEach var="reference" items="${referenceList}">
        <tr>
            <td><zfin:link entity="${reference.term}"/></td>
            <td>${reference.term.oboID}</td>
            <td>${reference.reference}</td>
        </tr>
    </c:forEach>
</table>

