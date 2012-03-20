<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.util.database.presentation.UnloadBean" scope="request"/>

<b>Tables and Unload Dates:</b>

<table>
    <tr>
        <td>Index File</td>
        <td>${formBean.unloadService.indexDirectory}</td>
    </tr>
    <tr>
        <td>Unloads Directory</td>
        <td><%=ZfinPropertiesEnum.DATABASE_UNLOAD_DIRECTORY %>
        </td>
    </tr>
</table>

<p/>
<a href="re-load-index">Re-load Index:</a>

<p/>
Number of Documents: <fmt:formatNumber type="number" pattern="###,###"
                                       value="${formBean.unloadService.numberOfDocuments}"/>

<table class="summary rowstripes">
    <tr>
        <th class="sectionTitle">Indexed Tables</th>
        <th class="sectionTitle">First Unload Date</th>
        <th class="sectionTitle">Last Unload Date</th>
        <th class="sectionTitle">Upgrade to latest Date: ${formBean.unloadIndexingService.latestUnloadDate}</th>
    </tr>
    <c:forEach var="tableSummary" items="${formBean.unloadService.tableSummaryList}" varStatus="loop">
        <zfin:alternating-tr groupBeanCollection="${formBean.unloadService.tableSummaryList}" loopName="loop">
            <td class="listContent">
                <a href="table-summary/${tableSummary.tableName}">${tableSummary.tableName}</a><br/>
            </td>
            <td class="listContent">
                <c:out value='${tableSummary.dateFirstAppearance}'/>
            </td>
            <td class="listContent">
                <c:out value='${tableSummary.dateLastAppearance}'/>
            </td>
            <td class="listContent">
                <a href="/action/unload/upgrade-table-index/${tableSummary.tableName}" style="">Upgrade</a>

            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

<p/>

<form:form method="Get" action="/action/unload/date-history" modelAttribute="formBean" name="Date History"
           id="Date History" onsubmit="return false;">
    Find History for Date <form:select path="date" items="${formBean.dateList}"/>
    <input value="Search" onclick="document.getElementById('Date History').submit();" type="button">
</form:form>

<form:form method="Post" action="/action/unload/index-new-table" modelAttribute="formBean" name="Index New Table"
           id="Index New Table" onsubmit="return false;">
    Index table <form:select path="tableName" items="${formBean.unIndexedTables}"/>
    <input value="Index" onclick="document.getElementById('Index New Table').submit();" type="button"> from Date on
    <form:select path="date" items="${formBean.dateList}"/>
</form:form>


