<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="90%">

    <tr><td colspan="3" class="sectionTitle">Hibernate Statistics Information</td></tr>
    <tr>
        <td class="sectionTitle">Key</td>
        <td width="120" colspan="2" class="sectionTitle">value</td>
    </tr>
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
    <tr>
        <td valign=top class="listContentBold">
            Entities Inserted:
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.entityInsertCount}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Entities Deleted:
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.entityDeleteCount}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Entities Updated:
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.entityUpdateCount}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Entities Loaded:
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.entityLoadCount}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Entities Fetched:
        </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.statistics.entityFetchCount}"/>
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
