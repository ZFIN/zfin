<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>

<table class="table-results searchresults" style="display: none;">
    <th>Symbol</th>
    <th>Name</th>
    <th>Previous Names</th>
    <th>Marker Type</th>
    <th>Location</th>
    <th>ID</th>
    <th>Related Data</th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>
            <td>${result.fullName}</td>
            <td>
                <c:forEach var="alias" items="${result.entity.aliases}" varStatus="loop">
                    ${alias.alias}<c:if test="${!loop.last}">, </c:if>
                </c:forEach>
            </td>
            <td>${result.type}</td>
            <td><zfin2:displayLocation entity="${result.entity}" longDetail="false"/></td>
            <td style="white-space: nowrap"> <c:if test="${!empty result.displayedID}">${result.id}</c:if> </td>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>