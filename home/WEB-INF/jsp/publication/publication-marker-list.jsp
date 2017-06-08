<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-figure:publicationInfo publication="${publication}"
                             showThisseInSituLink="false"
                             showErrataAndNotes="false"/>

<table class="summary rowstripes" style="margin-top: 1em;">
    <caption>
        <zfin:choice choicePattern="0#Genes / Markers| 1#Gene / Marker| 2#Genes / Markers"
                     integerEntity="${fn:length(markers)}"
                     includeNumber="true"/>
    </caption>
    <tr>
        <th>Marker Type</th>
        <th>Symbol</th>
        <th>Name</th>
    </tr>
    <c:forEach var="marker" items="${markers}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>${marker.markerType.displayName}</td>
            <td><zfin:link entity="${marker}"/></td>
            <td>${marker.name}</td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
