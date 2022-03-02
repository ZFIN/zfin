<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${ statistics != null}">
    <table class="table table-bordered" style="vertical-align: top">
        <caption>${statistics.database.name}</caption>
        <tr>
            <td>Sequences</td>
            <td><fmt:formatNumber value="${statistics.numSequences}" type="number"/></td>
        </tr>
        <tr>
            <td>Date Created</td>
            <td><fmt:formatDate value="${statistics.creationDate}" pattern="yyyy/MM/dd"/></td>
        </tr>
        <tr>
            <td>Description</td>
            <td>${statistics.database.description}</td>
        </tr>
    </table>
</c:if>