<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%--
<jsp:useBean id="session" class="org.zfin.database.SysSession" scope="request"/>
--%>
<z:devtoolsPage>
    <h3>
        Session View:
    </h3>

    <a href="/action/database/all-sessions">Back to overview</a>


    <table width="80%" class="rowstripes summary">
        <tr>
            <td width="150">ID</td>
            <td>${session.sid}</td>
        </tr>
        <tr>
            <td>Username</td>
            <td>${session.userName}</td>
        </tr>
        <tr>
            <td>Instance</td>
            <td>${session.sysOpenDb.name}</td>
        </tr>
        <tr>
            <td>Connected since</td>
            <td>${zfn:getTimeDurationToday(session.startDate)}</td>
        </tr>
        <tr>
            <td>Connection Date</td>
            <td><fmt:formatDate value="${session.startDate}" type="date" timeZone="PST"/></td>
        </tr>
        <tr>
            <td>Connection Time</td>
            <td><fmt:formatDate value="${session.startDate}" pattern="hh:mm:ss"/></td>
        </tr>
        <tr>
            <td>PID</td>
            <td>${session.pid}</td>
        </tr>
        <tr>
            <td>UID</td>
            <td>${session.uid}</td>
        </tr>
        <tr>
            <td>TTY</td>
            <td>${session.tty}</td>
        </tr>
        <tr>
            <td>Pool Address</td>
            <td>${session.poolAddress}</td>
        </tr>
        <tr>
            <td>States</td>
            <td>
                <c:forEach var="state" items="${session.listOfStates}">
                    ${state.message}
                </c:forEach>
            </td>
        </tr>
        <tr>
            <td>Flag Status</td>
            <td>
                <c:forEach var="flagStatus" items="${session.flagStatus}">
                    ${flagStatus}
                </c:forEach>
            </td>
        </tr>
    </table>

    <h3>
        Locks associated to this session:
    </h3>

    <table width="80%" class="rowstripes summary">
        <tr>
            <th>ID</th>
            <th><a href="?orderBy=sid">Dbs Name</a></th>
            <th><a href="?orderBy=userName">Table Name</a></th>
            <th><a href="?orderBy=userName">Type</a></th>
            <th><a href="?orderBy=userName">Row ID (if index key lock)</a></th>
            <th><a href="?orderBy=userName">key Num</a></th>
            <th><a href="?orderBy=userName">Session waiting for this lock</a></th>
        </tr>
        <c:forEach var="lock" items="${session.syslocks}" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${syslocks}">
                <td>${loop.index +1}</td>
                <td>${lock.dbsName}</td>
                <td>${lock.tableName}</td>
                <td>${lock.lockType.name}</td>
                <td>${lock.rowId}</td>
                <td>${lock.keyNum}</td>
                <td><a href="/action/database/view-session/${lock.waiter}">${lock.waiter}</a></td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>


    <style type="text/css">
        h3 {
            font-variant: small-caps;
        }
    </style>
</z:devtoolsPage>