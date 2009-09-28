<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<h3>Hibernate Statistics Information</h3>


<h4>Connections:</h4>
<a href="/action/dev-tools/view-hibernate-statistics?reset=true">reset</a>
<table cellpadding="2" cellspacing="1" border="0" width="90%">
    <tr>
        <td valign=top class="listContentBold">
            Sessions open - closed: 
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.sessionOpenCount - formBean.statistics.sessionCloseCount}"/>
        </td>
    </tr>

    <tr>
        <td valign=top class="listContentBold">
            Number of Connections from Session object (these connections may be pooled).
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.connectCount}"/>
        </td>
    </tr>
</table>

<h4>Caching:</h4>
<table cellpadding="2" cellspacing="1" border="0" width="90%">
    <tr>
        <td valign=top class="listContentBold">
            Prepared Statement Count:
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.prepareStatementCount}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            First-level Query Cache hit/miss/put/excecution:
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.queryCacheHitCount}"/>
            /
            <c:out value="${formBean.statistics.queryCacheMissCount}"/>
            /
            <c:out value="${formBean.statistics.queryCachePutCount}"/>
            /
            <c:out value="${formBean.statistics.queryExecutionCount}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Second-level Query Cache hit/miss/put:
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.secondLevelCacheHitCount}"/>
            /
            <c:out value="${formBean.statistics.secondLevelCacheMissCount}"/>
            /
            <c:out value="${formBean.statistics.secondLevelCachePutCount}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Entities Inserted / Deleted / Updated / Loaded / Fetched
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.entityInsertCount}"/>
            /
            <c:out value="${formBean.statistics.entityDeleteCount}"/>
            /
            <c:out value="${formBean.statistics.entityUpdateCount}"/>
            /
            <c:out value="${formBean.statistics.entityLoadCount}"/>
            /
            <c:out value="${formBean.statistics.entityFetchCount}"/>
        </td>
    </tr>
</table>


<h4>Queries:</h4>
<table cellpadding="2" cellspacing="1" border="0" width="90%">
    <tr>
        <td valign=top class="listContentBold">
            Slowest Query:
        </td>
        <td colspan="2" class="listContentBold">
            Time [ms]:
        </td>
    </tr>
    <tr>
        <td valign=top class="listContent">
            <c:out value="${formBean.statistics.queryExecutionMaxTimeQueryString}"/>
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.queryExecutionMaxTime}"/>
        </td>
    </tr>

    <tr><td colspan="3" class="sectionTitle">Individual Queries</td></tr>
    <tr>
        <td class="sectionTitle">Query</td>
        <td width="120" colspan="2" class="sectionTitle">Statistics</td>
    </tr>
    <c:forEach var="query" items="${formBean.categoryQuery}">
        <tr>
            <td class="listContent">
                <c:out value='${query.key}'/>
            </td>
            <td colspan="2" class="listContent"> Execution
            </td>
            <td></td>
        </tr>
        <tr class="listContent">
            <td></td>
            <td>Count:</td>
            <td align="right"><c:out value='${query.value.executionCount}'/></td>
        </tr>
        <tr class="listContent">
            <td></td>
            <td>Average Time:</td>
            <td align="right"><c:out value='${query.value.executionAvgTime}'/></td>
        </tr>
        <tr class="listContent">
            <td></td>
            <td>Min Time:</td>
            <td align="right"><c:out value='${query.value.executionMinTime}'/></td>
        </tr>
        <tr class="listContent">
            <td></td>
            <td>Max Time:</td>
            <td align="right"><c:out value='${query.value.executionMaxTime}'/></td>
        </tr>
        <tr class="listContent">
            <td></td>
            <td>Row Count:</td>
            <td align="right"><c:out value='${query.value.executionRowCount}'/></td>
        </tr>
    </c:forEach>
</table>
