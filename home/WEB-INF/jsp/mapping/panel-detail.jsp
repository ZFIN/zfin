<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="primary-entity-attributes">
    <tr>
        <th width="100"><span class="name-label">Panel Name:</span></th>
        <td><span class="name-label"> ${panel.name} (${panel.abbreviation})</span></td>
    </tr>
    <tr>
        <th>Description:</th>
        <td>
            ${panel.comments}
        </td>
    </tr>
    <tr>
        <th>Panel Producer:</th>
        <td><zfin:link entity="${panel.producer}"/>
        </td>
    </tr>
    <tr>
        <th>Panel Type:</th>
        <td>${panel.type}
        </td>
    </tr>
    <tr>
        <th>Current source of <br/>genetic material for mapping:</th>
        <td>
            <zfin:link entity="${panel.source}"/>
        </td>
    </tr>
    <tr>
        <th>Most Recent Update:</th>
        <td><fmt:formatDate type="date" value="${panel.date}"/></td>
    </tr>
    <tr>
        <c:choose>
            <c:when test="${panel.type eq 'Meiotic'}">
                <th>Number of Meioses:</th>
                <td>${panel.numberOfMeioses} </td>
            </c:when>
            <c:otherwise>
                <th>Radiation Dose:</th>
                <td>${panel.radiationDose} </td>
            </c:otherwise>
        </c:choose>
    </tr>
    <c:if test="${panel.abbreviation eq 'LN54'}">
        <th>Interactive Mapping:</th>
        <td><a href="/action/mapping/ln54mapper">Place markers on LN54 panel</a></td>
    </c:if>

</table>
<div class="summary">
    <table id="meioticPanel" class="summary horizontal-solidblock">
        <caption>PANEL STATISTICS</caption>
        <tr>
            <th style="width: 25px">Markers</th>
            <th>Chromosome</th>
        </tr>
        <tr>
            <td style="width: 10%">
                <table class="summary1">
                    <tr>
                        <th style="width: 10%">Marker Type</th>
                        <th style="text-align: right">Count</th>
                        <th style="text-align: right"></th>
                    </tr>
                    <c:forEach var="chromosomePanelCount" items="${panel.panelMarkerCountMap}">
                        <tr>
                            <td>${chromosomePanelCount.key}</td>
                            <td style="text-align: right"><fmt:formatNumber value="${chromosomePanelCount.value}"
                                                                            type="number"/></td>
                        </tr>
                    </c:forEach>
                    <tr style="font-weight: bold">
                        <td>Total</td>
                        <td style="text-align: right"><fmt:formatNumber value="${panel.panelMarkerCount}"
                                                                        type="number"/></td>
                    </tr>
                </table>
                <table>
                    <tr>
                        <td>Click "Map" to see a graphical map of an individual chromosome</td>
                    </tr>
                    <c:if test="${panel.abbreviation eq 'ZMAP'}">
                        <tr>
                            <td><span class="bold">Note:</span>There is too much data to display an entire chromosome
                                Please use <a href="/cgi-bin/view_zmapplet.cgi?marker=">Search ZMap</a> to restrict the
                                query.
                            </td>
                        </tr>
                    </c:if>
                </table>
            </td>
            <td style="text-align: left; width: 50%">
                <table class="summary" style="font-size: 11px; text-align: left; width: 55%">
                    <tr>
                        <th style="width: 30px">Chromsome</th>
                        <th style=" width: 60%">Marker Type (# on Chr.)</th>
                        <th>View</th>
                        <th>Scoring</th>
                    </tr>
                    <c:forEach var="chromosomePanelCount" items="${panel.chromosomePanelCountMap}">
                        <tr>
                            <td>${chromosomePanelCount.key}</td>
                            <td>
                                <c:forEach var="markerCount" items="${chromosomePanelCount.value}">
                                    ${markerCount.key}(${markerCount.value})
                                </c:forEach>
                            </td>
                            <td>
                                <a href="/cgi-bin/view_mapplet.cgi?loc_lg=${chromosomePanelCount.key}&loc_panel=${panel.abbreviation}&view_map=viewmap&userid=GUEST">
                                    Map
                                </a>
                            </td>
                            <td>
                                <a href="/action/mapping/show-scoring?panelID=${panel.zdbID}&lg=${chromosomePanelCount.key}"
                                   target="_scoring">Data</a></td>
                        </tr>
                    </c:forEach>
                </table>
            </td>
        </tr>
    </table>

    <SCRIPT>
        function call_mapplet(lg, panel_abbrev) {
            for (var count = 0; count <= 25; count++) {
                if (lg == count) document.mapview.loc_lg.value = count;

            }
            document.mapview.submit();
        }
    </SCRIPT>

    List of all <a href="/action/mapping/all-panels"> mapping panels</a>

    <p/>
    <a href="/action/publication/list/${panel.zdbID}">CITATIONS</a> (${citationCount})

</div>