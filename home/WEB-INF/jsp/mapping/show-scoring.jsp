<%@ page import="java.util.Date" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<p/>

<div class="titlebar">
    <h1>Scoring Data</h1>
</div>

<p/>

<table id="geneticMapping" class="summary horizontal-solidblock">
    <caption>
        <jsp:useBean id="today" class="java.util.Date" scope="page"/>
        PANEL: ${mappedMarkerList[0].panel.name} (${mappedMarkerList[0].panel.abbreviation}),
        Chr ${mappedMarkerList[0].lg}
        <c:if test="${mappedMarkerList.size() == 1}">
            , ${mappedMarkerList[0].entityAbbreviation}
        </c:if>
        as of <fmt:formatDate value="${today}" pattern="YYYY-MM-dd HH:mm:ss"/>

    </caption>
    <tr>
        <th style="width: 10%">Name</th>
        <th style="width: 10%">Location</th>
        <th>Scoring Vector</th>
    </tr>
    <c:forEach var="mappedMarker" items="${mappedMarkerList}">
        <tr>
            <td style="width: 10%">${mappedMarker.entityAbbreviation}</td>
            <td style="width: 10%">${mappedMarker.lgLocation} ${mappedMarker.metric}</td>
            <td style="width: 80%">${mappedMarker.scoringData}</td>
        </tr>
    </c:forEach>
</table>

