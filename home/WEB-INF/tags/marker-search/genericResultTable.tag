<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="searchresults">
    <caption>${criteria.numFound} results</caption>
    <tr>
        <th>Name</th>
        <th>Type</th>
        <th>Matching Text</th>
    </tr>
    <c:forEach var="result" items="${criteria.results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${criteria.results}" groupByBean="marker.zdbID">
            <td>
                <zfin:link entity="${result.marker}"/>
            </td>
            <td>
                ${result.marker.markerType.displayName}
            </td>
            <td>
                    ${result.matchingText}
            </td>
        </zfin:alternating-tr>
    </c:forEach>

</table>