<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="siteSearchIndexService" class="org.zfin.uniquery.SiteSearchIndexService" scope="request"/>
<jsp:useBean id="formBean" class="org.zfin.util.database.presentation.UnloadBean" scope="request"/>

<b>Site Search Indexes: </b>

<p></p>
<table class="primary-entity-attributes summary">
    <tr>
        <th nowrap="nowrap" width="120">Index Root Archive Directory</th>
        <td>${siteSearchIndexService.rootArchiveDirectory}
        </td>
    </tr>
    <tr>
        <th>Latest Archive</th>
        <td>${siteSearchIndexService.latestUnloadDate}</td>
    </tr>
    <tr>
        <th>Unload Date</th>
        <td>
            <fmt:formatDate value="${siteSearchIndexService.unloadInfo.date}" pattern="yyyy.MM.dd"/>
        </td>
    </tr>
    <tr>
        <th>Number of Documents</th>
        <td>
            <fmt:formatNumber type="number" pattern="###,###" value="${siteSearchIndexService.numberOfDocuments}"/>
        </td>
    </tr>
    <tr>
        <th>Archive Currently Used</th>
        <td>${siteSearchIndexService.matchingIndexDirectory}</td>
    </tr>
    <tr>
        <th>Update Cache</th>
        <td><a href="update-site-search-cache"> Update </a>
        </td>
    </tr>
</table>


<form:form method="Get" modelAttribute="formBean" name="Date History" onsubmit="return false;">
    Show archive for <form:select path="date" items="${formBean.indexDateList}"/>
</form:form>

