<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div>
    <input type="radio" id="pet-date" name="query" checked><label for="pet-date">PET Date</label>
    <input type="radio" id="status-date" name="query"><label for="status-date">Status Change Date</label>
    <input type="radio" id="cumulative" name="query"><label for="cumulative">Cumulative Stats</label>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>From</th>
        <td>
            <input id="from-date">
            <b>To</b>
            <input id="to-date">
            <b>By</b>
            <select id="date-group">
                <option>Year</option>
                <option>Month</option>
                <option>Day</option>
            </select>
        </td>
    </tr>

    <tr>
        <th>Show</th>
        <td>
            <div style="column-count: 2">
                <div><input type="checkbox" id="stat-count"> <label for="stat-count">Count</label></div>
                <div><input type="checkbox" id="stat-avg"> <label for="stat-avg">Average Days in Status</label></div>
                <div><input type="checkbox" id="stat-std"> <label for="stat-std">Standard Deviation Days in Status</label></div>
                <div><input type="checkbox" id="stat-min"> <label for="stat-min">Minimum Days in Status</label></div>
                <div><input type="checkbox" id="stat-max"> <label for="stat-max">Maximum Days in Status</label></div>
            </div>
        </td>
    </tr>

    <tr>
        <th>Status</th>
        <td>
            <div style="column-count: 2">
                <c:forEach items="${statuses}" var="status">
                    <div style="-webkit-column-break-inside: avoid; page-break-inside: avoid; break-inside: avoid;">
                        <input type="checkbox" name="status" id="${status.name}"> <label for="${status.name}">${status.name.display}</label>
                        <c:if test="${status.name == 'READY_FOR_INDEXING'}">
                            <div style="padding-left: 10px">
                                <c:forEach items="${indexingLocations}" var="location">
                                    <div><input type="checkbox" name="location" id="${location.name}"> <label for="${location.name}">${location.name.display}</label></div>
                                </c:forEach>
                            </div>
                        </c:if>
                        <c:if test="${status.name == 'READY_FOR_CURATION'}">
                            <div style="padding-left: 10px">
                                <c:forEach items="${curatingLocations}" var="location">
                                    <div><input type="checkbox" name="location" id="${location.name}"> <label for="${location.name}">${location.name.display}</label></div>
                                </c:forEach>
                            </div>
                        </c:if>
                    </div>
                </c:forEach>
            </div>
        </td>
    </tr>
</table>

<div>
    <input type="checkbox" id="current-only"> <label for="current-only">Current status only</label>
</div>

<div>
    <button type="submit">Submit</button>
</div>