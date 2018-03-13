<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="mappedMarkers" required="true" type="java.util.List" %>
<%@ attribute name="targetEntity" required="true" type="org.zfin.infrastructure.ZdbID" %>
<%@ attribute name="hideTitle" required="false" type="java.lang.Boolean" %>

<table id="geneticMapping" class="summary rowstripes">
    <c:if test="${!hideTitle}">
        <caption>GENETIC MAPPING PANELS</caption>
    </c:if>
    <tr>
        <th style="width: 10%">Chr</th>
        <th style="width: 10%">Location</th>
        <th style="width: 15%">Mapped As</th>
        <th style="width: 35%">Panel</th>
        <th style="width: 15%">Mapped By</th>
        <th style="width: 15%">Scoring</th>
    </tr>
    <c:forEach var="mappedMarker" items="${mappedMarkers}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>${mappedMarker.lg}</td>
            <td>${mappedMarker.lgLocation} ${mappedMarker.metric}</td>
            <td>${mappedMarker.mappedName}</td>
            <td><zfin:link entity="${mappedMarker.panel}"/></td>
            <td><zfin:link entity="${mappedMarker.owner}"/></td>
            <td>
                <a href="/action/mapping/show-scoring?panelID=${mappedMarker.panel.zdbID}&markerID=${mappedMarker.entityID}&lg=${mappedMarker.lg}"
                   target="_scoring">Data</a>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
    <tr>
        <td colspan="6">

        </td>
    </tr>
    <tr>
        <td colspan="4" style="font-size: 12px"><span class="bold">Note:</span> Physical map location as
            displayed in the genome browsers is more precise than linkage map location. <br/>
            <span class="bold" style="visibility: hidden">Note:</span> Physical map location should be used whenever possible.
        </td>
    </tr>
</table>
