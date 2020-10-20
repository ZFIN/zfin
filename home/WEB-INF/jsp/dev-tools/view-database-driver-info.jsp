<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="pds" type="com.mchange.v2.c3p0.ComboPooledDataSource" scope="request" />

<z:devtoolsPage title="Database Driver Info">
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
                <table>
                    <tr>
                        <td>Max Number of Connections:</td>
                        <td>${pds.maxPoolSize}</td>
                    </tr>
                    <tr>
                        <td>Min Number of Connections:</td>
                        <td>${pds.minPoolSize}</td>
                    </tr>
                    <tr>
                        <td>Number of Connections in the Pool:</td>
                        <td>${pds.numConnectionsDefaultUser}</td>
                    </tr>
                    <tr>
                        <td>Number of Busy Connections:</td>
                        <td>${pds.numBusyConnectionsDefaultUser}</td>
                    </tr>
                    <tr>
                        <td>Number of Idle Connections:</td>
                        <td>${pds.numIdleConnectionsDefaultUser}</td>
                    </tr>
                    <tr>
                        <td>Number of Thread pool active threads:</td>
                        <td>${pds.threadPoolNumActiveThreads}</td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>

    Current DB connections: <a href="#" onclick="clearTable()">Clear Table</a>
    <br/>
    <b>Note:</b> One DB connection is used for this page / info update!
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
</z:devtoolsPage>