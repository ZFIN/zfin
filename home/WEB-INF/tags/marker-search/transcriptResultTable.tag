<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="criteria" type="org.zfin.search.presentation.MarkerSearchCriteria"%>

<table class="searchresults">
    <caption></caption>
    <tr>
        <th>Name</th>
        <th>Transcript type</th>
        <th>Map</th>
        <th>Matching Text</th>
    </tr>
    <c:forEach var="result" items="${criteria.results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${criteria.results}" groupByBean="marker.zdbID">
            <td><zfin:link entity="${result.marker}"/></td>
            <td>${result.marker.transcriptType.display}</td>
            <td><zfin2:displayLocation entity="${result.marker}"/></td>
            <td>${result.matchingText}</td>
        </zfin:alternating-tr>
    </c:forEach>

</table>