<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <table>
        <tr>
            <td><h2>Zebrafish Mapping Panels: Summary Listing</h2></td>
        </tr>
    </table>
    <p>


    <div class="summary">
        <table id="meioticPanel" class="summary horizontal-solidblock">
            <caption>MEIOTIC PANELS</caption>
            <tr>
                <th style="width: 25%">Name</th>
                <th style="width: 50%">Marker Types on Panel</th>
                <th style="width: 10%">Total Marker</th>
                <th style="width: 15%">Updated</th>
            </tr>
            <c:forEach var="panel" items="${meioticPanelList}">
                <tr>
                    <td><zfin:link entity="${panel}"/></td>
                    <td>
                        <c:forEach var="chromosomePanelCount" items="${panel.panelMarkerCountMap}" varStatus="index">
                            ${chromosomePanelCount.key}<c:if test="${!index.last}">, </c:if>
                        </c:forEach>
                    </td>
                    <td><fmt:formatNumber value="${panel.panelMarkerCount}" type="number"/></td>
                    <td align="left"><fmt:formatDate type="date" value="${panel.date}"/></td>
                </tr>
            </c:forEach>
        </table>
        <p/>
        <p/>
        <table id="radiationPanel" class="summary horizontal-solidblock">
            <caption>RADIATION HYBRID PANELS</caption>
            <tr>
                <th style="width: 25%">Name</th>
                <th style="width: 50%">Marker Types on Panel</th>
                <th style="width: 10%">Total Marker</th>
                <th style="width: 15%">Updated</th>
            </tr>
            <c:forEach var="panel" items="${radiationPanelList}">
                <tr>
                    <td><zfin:link entity="${panel}"/></td>
                    <td>
                        <c:forEach var="chromosomePanelCount" items="${panel.panelMarkerCountMap}" varStatus="index">
                            ${chromosomePanelCount.key}<c:if test="${!index.last}">, </c:if>
                        </c:forEach>
                    </td>
                    <td><fmt:formatNumber value="${panel.panelMarkerCount}" type="number"/></td>
                    <td align="left"><fmt:formatDate type="date" value="${panel.date}"/></td>
                </tr>
            </c:forEach>
        </table>
    </div>
</z:page>