<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<b>Site Search Indexes:</b>

<p/>
<table class="primary-entity-attributes summary">
    <tr>
        <td width="200">Index Unload Directory</td>
        <td>${formBean.indexDirectory}
        </td>
    </tr>
    <tr>
        <td>Latest Index Files</td>
        <td>${formBean.latestUnloadDate}</td>
    </tr>
    <tr>
        <td>Matching Index Files</td>
        <td>${formBean.matchingIndexDirectory}</td>
    </tr>
    <tr>
        <td>Unload Date</td>
        <td>
            <fmt:formatDate value="${formBean.unloadDate.date}" pattern="yyyy.MM.dd"/>
        </td>
    </tr>
    <tr>
        <td>Number of Documents:</td>
        <td>
            <fmt:formatNumber type="number" pattern="###,###" value="${formBean.numberOfDocuments}"/>
        </td>
    </tr>
</table>


