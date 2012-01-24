<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="table" type="org.zfin.database.presentation.Table" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td class="titlebar">
            <span class="name-label">View Table Statistics for: </span>
            <span style="color: red">
                ${table.tableName}
            </span>
        </td>
    </tr>
</table>

<p/>

<table class="summary groupstripes">
    <tr>
        <th>
            Column Name
        </th>
        <th>
            PK
        </th>
        <th>
            FK
        </th>
        <th>
            Column Type
        </th>
        <th>
            Column Length
        </th>
        <th>
            Nullable
        </th>
    </tr>
    <c:forEach var="column" items="${columnMetaData}" varStatus="row_index">
        <tr>
            <td width="20%" style="font-weight: bold">
                <c:if test="${column.foreignKey}"><span style="color: green;"></c:if>
                <c:if test="${column.primaryKey}"><span style="color: maroon;"></c:if>
                        ${column.name}</span>
            </td>
            <td width="5%">
                <c:if test="${column.primaryKey}">PK</c:if>
            </td>
            <td>
                <c:if test="${column.foreignKey}">
                    <table class="summary rowstripes">
                        <tr>
                            <th width="10%" nowrap="true">Table Name</th>
                            <th style="text-align: right"># of Records</th>
                        </tr>
                        <c:forEach var="tableRecord" items="${column.referenceTableRecordList}">
                            <tr>
                                <td>
                                    <a href="${tableRecord.table.tableName}">${tableRecord.table.tableName}</a>
                                </td>
                                <td style="text-align: right"><fmt:formatNumber value="${tableRecord.numberOfRecords}"
                                                                                pattern="##,###"/></td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:if>
            </td>
            <td width="20%">
                    ${column.columnType}
            </td>
            <td>
                    ${column.columnLength}
            </td>
            <td>
                    ${column.nullable}
            </td>
        </tr>
    </c:forEach>
</table>

<p/>

<table class="summary groupstripes">
    <tr>
        <th width="20%">
            Tables referencing ${table.tableName}
        </th>
        <td>
            <table>
                <c:forEach var="foreignKey" items="${table.fkReferences}">
                    <tr>
                        <td>
                            <a href="${foreignKey.foreignKeyTable.tableName}">${foreignKey.foreignKeyTable.tableName}</a>
                            [${foreignKey.foreignKey}]
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </td>
    </tr>
</table>
<p/>

<table class="summary groupstripes">
    <tr>
        <th width="20%">
            Number of Records
        </th>
        <td>
            <fmt:formatNumber value="${numberOfRecords}" pattern="##,###"/>
        </td>
    </tr>
</table>
<p/>

    <table class="summary groupstripes">
        <tr>
            <th width="20%">
                Histogram for Foreign Keys
            </th>
            <td>
                <table>
                    <tr>
                        <th>Column Name</th>
                        <th>Column Values</th>
                        <th># of Records</th>
                        <th>Query</th>
                    </tr>
                    <c:forEach var="dictionary" items="${foreignHistograms}">
                        <tr>
                            <td>
                                <span style="font-weight: bold;">${dictionary.columnName}</span>
                            </td>
                            <td style="text-align: center">${fn:length(dictionary.values)}</td>
                            <td><fmt:formatNumber value="${dictionary.numberOfRecords}" pattern="##,###"/></td>
                            <td>
                                    ${dictionary.query}
                            </td>
                        </tr>
                        <c:forEach var="dictionaryValue" items="${dictionary.values}" varStatus="loop">
                            <tr>
                                <td></td>
                                <td>
                                    <c:if test="${dictionaryValue.value eq ''}">[NULL]</c:if>
                                    <zfin2:create-record-link value="${dictionaryValue.value}" column="${dictionary.column}"
                                              identifier="${dictionary.column.name}-${loop.count}"/>
                                </td>
                                <td style="text-align: right">
                                    <fmt:formatNumber value="${dictionaryValue.numberOfValues}" pattern="##,###"/>
                                </td>
                                <td></td>
                            </tr>
                        </c:forEach>
                    </c:forEach>
                </table>
            </td>
        </tr>
    </table>
<p/>

<c:if test="${fn:length(dictionaryValuesList) > 0}">
    <table class="summary groupstripes">
        <tr>
            <th width="20%">
                Dictionary Values
            </th>
            <td>
                <table>
                    <tr>
                        <th>Column Name</th>
                        <th>Column Values</th>
                        <th># of Records</th>
                        <th>Query</th>
                    </tr>
                    <c:forEach var="dictionary" items="${dictionaryValuesList}">
                        <tr>
                            <td>
                                <span style="font-weight: bold;">${dictionary.columnName}</span>
                            </td>
                            <td style="text-align: center">${fn:length(dictionary.values)}</td>
                            <td><fmt:formatNumber value="${dictionary.numberOfRecords}" pattern="##,###"/></td>
                            <td>
                                    ${dictionary.query}
                            </td>
                        </tr>
                        <c:forEach var="dictionaryValue" items="${dictionary.values}">
                            <tr>
                                <td></td>
                                <td>
                                    <c:if test="${dictionaryValue.value eq ''}">[NULL]</c:if>
                                        ${dictionaryValue.value}
                                </td>
                                <td style="text-align: right">
                                    <fmt:formatNumber value="${dictionaryValue.numberOfValues}" pattern="##,###"/>
                                </td>
                                <td></td>
                            </tr>
                        </c:forEach>
                    </c:forEach>
                </table>
            </td>
        </tr>
    </table>
</c:if>
<p/>

<table class="summary groupstripes">
    <tr>
        <th width="20%">
            First 15 Records
        </th>
    </tr>
</table>

<zfin-dev:view-db-data-rows dataMap="${dataMap}"/>