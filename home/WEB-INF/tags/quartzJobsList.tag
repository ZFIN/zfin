<%@ tag import="org.zfin.framework.presentation.QuartzJobsBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="jobs" type="java.util.Collection" %>

<jsp:useBean id="formBean" class="org.zfin.framework.presentation.QuartzJobsBean" scope="request"/>

<table width="80%" class="rowstripes summary">
    <tr>
        <th><a href="/action/dev-tools/quartz-jobs?action=<%=QuartzJobsBean.Action.SORT_BY_GROUP.toString()%>">Group</a>
        </th>
        <th>Job Name</th>
        <th colspan="2">Last Run</th>
        <th colspan="2"><a
                href="/action/dev-tools/quartz-jobs?action=<%=QuartzJobsBean.Action.SORT_BY_TIME.toString()%>">Next
            Run</a></th>
        <th>Paused</th>
        <th>Running</th>
        <th>Action</th>
    </tr>
    <tr>
        <th></th>
        <th></th>
        <th>Date</th>
        <th>Time</th>
        <th>Date</th>
        <th>Time</th>
        <th></th>
        <th></th>
        <th></th>
    </tr>
    <c:forEach var="jobBean" items="${jobs}" varStatus="loop">

        <c:choose>
            <c:when test="${loop.index %2 == 0}">
                <tr class="even">
            </c:when>
            <c:otherwise>
                <tr class="odd">
            </c:otherwise>
        </c:choose>
        <td nowrap="true">
            <c:if test="${jobBean.group != lastGroup}">
                <strong>${jobBean.group}</strong>
            </c:if>
            <c:set var="lastGroup" value="${jobBean.group}"/>
        </td>
        <td>
                ${jobBean.name}
        </td>
        <td>
            <zfin2:displayDay date="${jobBean.lastExecution}" />
        </td>
        <td>
            <fmt:formatDate value="${jobBean.lastExecution}" pattern="HH:mm:ss"/>
        </td>
        <td>
            <zfin2:displayDay date="${jobBean.nextExecution}" />
        </td>
        <td>
            <fmt:formatDate value="${jobBean.nextExecution}" pattern="HH:mm:ss"/>
        </td>
        <td>
                ${jobBean.paused ? "YES": "NO"}
        </td>
        <td>
            <c:if test="${jobBean.running}">
                <span class="red">running ...</span>
            </c:if>
        </td>
        <td>
            <a href="?action=<%=QuartzJobsBean.Action.RUN%>&job=${jobBean.name}&group=${jobBean.group}&requestID=${requestID}">Run</a>
            <c:choose>
                <c:when test="${jobBean.paused}">
                    <a href="?action=<%=QuartzJobsBean.Action.RESUME%>&job=${jobBean.name}&group=${jobBean.group}&requestID=${requestID}">Resume</a>
                </c:when>
                <c:otherwise>
                    <a href="?action=<%=QuartzJobsBean.Action.PAUSE%>&job=${jobBean.name}&group=${jobBean.group}&requestID=${requestID}">Pause</a>
                </c:otherwise>
            </c:choose>
        </td>
        </tr>
    </c:forEach>
</table>
