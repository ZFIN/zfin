<%@ page import="com.mchange.v2.c3p0.ComboPooledDataSource" %>
<%@ page import="javax.naming.InitialContext" %>
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
        <td class="listContent" valign="top">
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

    <tr>
        <td valign="top">DB Connection Pool Info</td>
        <td>
            <%
                InitialContext ictx = new InitialContext();
                ComboPooledDataSource pds = (ComboPooledDataSource) ictx.lookup("java:comp/env/jdbc/zfin");
            %>
            <table>
                <tr>
                    <td>Number of Connections:</td>
                    <td><%=pds.getNumConnectionsDefaultUser()%>
                    </td>
                </tr>
                <tr>
                    <td>Number of Busy Connections:</td>
                    <td><%=pds.getNumBusyConnectionsDefaultUser()%>
                    </td>
                </tr>
                <tr>
                    <td>Number of Idle Connections:</td>
                    <td><%=pds.getNumIdleConnectionsDefaultUser()%>
                    </td>
                </tr>
                <tr>
                    <td>Number of Thread pool active threads:</td>
                    <td><%=pds.getThreadPoolNumActiveThreads()%>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

Current DB connections: <a href="#" onclick="clearTable()">Clear Table</a>
<table id="connections" class="summary" style="width: 250px">
    <thead>
    <tr>
        <th>Total</th>
        <th>Busy</th>
        <th>Idle</th>
    </tr>
    </thead>
    <tbody></tbody>
</table>

<script type="text/javascript">
    setInterval(function () {
        $.ajax({
            type: 'GET',
            url: '/action/devtool/db-connections',
            dataType: 'json',
            success: function (response) {
                jQuery("#connections tbody").append('<tr><td>' + response.totalNumber + '</td><td>' + response.busyNumber + '</td><td>' + response.idleNumber + '</td></tr>')
            }
        });
    }, 5000);

    function clearTable() {
        jQuery("#connections tbody").empty();
    }
</script>
