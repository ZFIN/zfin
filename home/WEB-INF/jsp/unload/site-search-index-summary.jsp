<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<b>Site Search Indexes:</b>

<table>
    <tr>
        <td>Index Unload Directory</td>
        <td>${formBean.indexDirectory}
        </td>
    </tr>
    <tr>
        <td>Latest Index Files</td>
        <td>${formBean.latestUnloadDate}</td>
    </tr>
</table>

<p/>
Number of Documents: <fmt:formatNumber type="number" pattern="###,###" value="${formBean.numberOfDocuments}"/>

