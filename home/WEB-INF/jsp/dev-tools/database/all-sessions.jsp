<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="70%">
    <tr>
        <td colspan="3" class="sectionTitle"><b>All Sessions: ${fn:length(sessions)}</b></td>
    </tr>
    <tr>
        <td><a href="overview">Back to overview</a></td>
        <td>&nbsp;</td>
    </tr>
</table>

<table>
    <tr>
        <td>Show Connections:</td>
        <td><a href="?dbname=<%= ZfinPropertiesEnum.DB_NAME %>">Mine |</a></td>
        <td><a href="?">All |</a></td>
        <td>
            <form:form method="Get" action="all-sessions" modelAttribute="formBean" name="dbname"
                       id="dbname" onsubmit="return false;">
                <form:select path="dbname" items="${dbnameList}"/>
                <input value="Search" onclick="document.getElementById('dbname').submit();" type="button">
            </form:form>
        </td>
    </tr>
</table>

<table width="80%" class="rowstripes summary">
    <tr>
        <th>ID</th>
        <th><a href="?orderBy=sid">Session ID</a></th>
        <th><a href="?orderBy=userName">User Name</a></th>
        <th><a href="?orderBy=instance">Instance</a></th>
        <%--
                <th><a href="?orderBy=uid">UID</a></th>
                <th><a href="?orderBy=pid">PID</a></th>
                <th><a href="?orderBy=hostname">Hostname</a></th>
        --%>
        <th><a href="?orderBy=connected">Connection Created </a></th>
        <th><a href="?orderBy=connected">Date Started</a></th>
        <th><a href="?orderBy=connected">Time Started</a></th>
    </tr>
    <c:forEach var="session" items="${sessions}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${sessions}">
            <td>${loop.index +1}</td>
            <td><a href="view-session/${session.sid}">${session.sid}</a></td>
            <td><a href="">${session.userName}</a></td>
            <td><a href="">${session.sysOpenDb.name}</a></td>
            <td>${zfn:getTimeDurationToday(session.startDate)}</td>
            <td><a href=""><fmt:formatDate value="${session.startDate}" type="date" timeZone="PST"/> </a></td>
            <td><a href=""><fmt:formatDate value="${session.startDate}" pattern="hh:mm:ss"/> </a></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
