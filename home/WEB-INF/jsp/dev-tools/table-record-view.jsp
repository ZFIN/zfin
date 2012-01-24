<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%--
<jsp:useBean id="table" class="org.zfin.database.presentation.Table" scope="request"/>
--%>
<jsp:useBean id="ID" class="java.lang.String" scope="request"/>
<jsp:useBean id="query" class="org.zfin.util.DatabaseJdbcStatement" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td class="titlebar">
            <span class="name-label">View Records of: </span>
            <span style="color: red">
                <a href="/action/database/view-table-statistics/${table.tableName}">${fn:toUpperCase(table.tableName)}</a>
            </span>
        </td>
    </tr>
</table>
<p/>
<table class="primary-entity-attributes">
    <tr>
        <th>
            Foreign Key Tables
        </th>
        <td>
            <c:if test="${fn:length(foreignKeyResultList ) > 0}">
                <table class="summary rowstripes">
                    <tr>
                        <th>Table Name</th>
                        <th>FK name</th>
                        <th style="text-align: right"># of Records</th>
                    </tr>
                    <c:forEach var="foreignKeyResult" items="${flattenedForeignKeyResultList}" varStatus="loop">
                        <c:set var="foreignKey" value="${foreignKeyResult.foreignKey}"/>
                        <zfin:alternating-tr loopName="loop" groupBeanCollection="${flattenedForeignKeyResultList}"
                                             groupByBean="rootForeignKey.foreignKeyTable.tableName">
                            <td style="text-indent: ${foreignKeyResult.level *20}px">
                                <c:choose>
                                    <c:when test="${(ID eq null) || (ID eq '')}">
                                        <a href="/action/database/view-records/<c:out value="${foreignKey.foreignKeyTable.tableName}"/>?columnName=${formBean.columnName[0]}&columnValue=${formBean.columnValue[0]}&foreignKeyName=${foreignKey.foreignKey}&fullNodeName=${formBean.fullNodeNameForNextForeignKey}${foreignKeyResult.fullNodeName}">
                                            <c:out value="${fn:toUpperCase(foreignKey.foreignKeyTable.tableName)}"/></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="/action/database/view-records/<c:out value="${foreignKey.foreignKeyTable.tableName}"/>?columnName=${foreignKey.foreignKey}&columnValue=${ID}&foreignKeyName=${foreignKey.foreignKey}&fullNodeName=${formBean.fullNodeNameForNextForeignKey}${foreignKeyResult.fullNodeName}">
                                            <c:out value="${fn:toUpperCase(foreignKey.foreignKeyTable.tableName)}"/></a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                    ${zfn:getForeignKeyColumn(foreignKey )}
                            </td>
                            <td style="text-align: right">
                                <fmt:formatNumber value="${foreignKeyResult.numberOfResults}" pattern="##,###"/>
                            </td>
                        </zfin:alternating-tr>
                    </c:forEach>
                </table>
            </c:if>
        </td>
    </tr>
    <tr>
        <th>
            Number of Rows
        </th>
        <td>
            ${totalRecords}
        </td>
    </tr>
    <tr>
        <th>
            Query
        </th>
        <td>
            ${query.query}
        </td>
    </tr>
</table>
<p/>


<zfin-dev:view-db-data-rows dataMap="${dataMap}"/>