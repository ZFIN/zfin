<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="80%">
    <tr>
        <td colspan=2 class="sectionTitle">Database Driver Info</td>
    </tr>

    <tr>
        <td class="listContent" width="120">
            JDBC Driver Vendor
        </td>
        <td class="listContent">
            <c:out value="${metadata.driverName}"/>
        </td>
    </tr>

    <tr>
        <td class="listContent">
            Driver Version
        </td>
        <td class="listContent">
            <c:out value="${metadata.driverVersion}"/>
        </td>
    </tr>

    <tr>
        <td class="listContent">
            Driver Info
        </td>
        <td class="listContent">
            <c:out value="${metadata.URL}"/>
        </td>
    </tr>

    <tr>
        <td class="listContent">
            Transaction Isolation Level
        </td>
        <td class="listContent">
            <c:out value="${metadata.connection.transactionIsolation}"/> <br>
            0: No transactions supported<br>
            1: Dirty reads are possible<br>
            2: No dirty reads<br>
            4: No dirty reads and no non-repeatable reads<br>
            4: No dirty reads and no non-repeatable reads and no phanotm reads<br>
        </td>
    </tr>

</table>

