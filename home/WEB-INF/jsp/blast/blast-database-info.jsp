<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

${statistics.database.name}
<table class="data-table table-fixed">
        <tr>
            <th>Number of Sequences</th>
            <td>${numSequences}</td>
        </tr>
        <tr>
            <th>Date Created</th>
            <td><fmt:formatDate value="${statistics.creationDate}" pattern="yyyy/MM/dd hh:mm"/></td>
        </tr>
        <tr>
            <th>Description</th>
            <td>${statistics.database.description}</td>
        </tr>
</table>