<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage>
    <script>
        dbname = ${param.dbname};
        state = ${param.active};
    </script>
    <table cellpadding="2" cellspacing="1" border="0" width="70%">
        <tr>
            <td><a href="/action/devtool/home">Back to overview</a></td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td colspan="3" class="sectionTitle"><b>All Sessions: ${fn:length(sessions)}</b></td>
        </tr>
    </table>

    <table>
        <tr>
            <td>Show Connections:</td>
    <%--
            <td>
                <c:choose><c:when test="${empty formBean.dbname }">
                    <a href="?dbname=${ZfinPropertiesEnum.DB_NAME}">My DB </a>
                </c:when><c:otherwise>
                    <a href="?dbname=">All</a>
                </c:otherwise></c:choose>
            </td>
            <td>
                <form:form method="Get" action="all-sessions" modelAttribute="formBean" name="dbname"
                           id="dbname" onsubmit="return false;">
                    <form:select path="dbname" items="${dbnameList}"/>
                    <input value="Search" onclick="document.getElementById('dbname').submit();" type="button">
                </form:form>
            </td>
        </tr>
        <tr>
            <td></td>
    --%>
            <td colspan="2">
                <c:choose><c:when test="${!formBean.active }">
                    <a href="?active=true">Active Connections only</a>
                </c:when><c:otherwise>
                    <a href="?active=false">All Connections </a>
                </c:otherwise></c:choose>
            </td>
        </tr>
    </table>

    <table width="80%" class="rowstripes summary">
        <tr>
            <th>ID</th>
            <th>PID</th>
            <th>Status</th>
            <th>DB Name</th>
            <th>Owner</th>
            <th>Connection Last Used</th>
            <th>Date Started</th>
            <th>Time Started</th>
            <th>Connection Created</th>
            <th>Last Query</th>
        </tr>
        <c:forEach var="session" items="${sessions}" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${sessions}">
                <td>${loop.index +1}</td>
                <td>${session.pid}</td>
                <td>${session.state}</td>
                <td>${session.dbname}</td>
                <td>${session.owner}</td>
                <td>${zfn:getTimeDurationToday(session.dateLastUsed)}</td>
                <td><fmt:formatDate value="${session.dateLastUsed}" type="date" timeZone="PST"/></td>
                <td><fmt:formatDate value="${session.dateLastUsed}" pattern="hh:mm:ss"/></td>
                <td>${zfn:getTimeDurationToday(session.dateCreated)}</td>
                <td>${session.query}</td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</z:devtoolsPage>