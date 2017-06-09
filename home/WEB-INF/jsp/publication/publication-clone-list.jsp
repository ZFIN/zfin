<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-figure:publicationInfo publication="${publication}"
                             showThisseInSituLink="false"
                             showErrataAndNotes="false"/>

<table class="summary rowstripes" style="margin-top: 1em;">
    <caption>
        Clone and Probe List (${pagination.totalRecords} Records)
    </caption>
    <tr>
        <th>Type</th>
        <th>Name</th>
        <th>Sequence Type</th>
        <th>Vector Type</th>
        <th>Map</th>
    </tr>
    <c:forEach var="clone" items="${clones}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>${clone.markerType.displayName}</td>
            <td><zfin:link entity="${clone}"/></td>
            <td>${clone.sequenceType}</td>
            <td>${clone.vector.type}</td>
            <td><zfin2:displayLocation entity="${clone}" /></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

<zfin2:pagination paginationBean="${pagination}" />
