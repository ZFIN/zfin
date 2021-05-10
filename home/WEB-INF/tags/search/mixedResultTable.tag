<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>

<table class="table-results searchresults" style="display: none;">
    <th>Name</th> <th>ID</th> <th>Category</th> <th>Type</th> <th></th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>
            <td style="white-space: nowrap"> <c:if test="${!empty result.displayedID}">${result.id}</c:if> </td>
            <td>${result.category}</td>
            <td>${result.type}</td>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>