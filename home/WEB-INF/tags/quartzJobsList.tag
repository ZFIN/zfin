<%@ tag import="org.zfin.framework.presentation.QuartzJobsBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="jobs" type="java.util.Collection" %>
<%@ attribute name="requestID" type="java.lang.String" %>


<table width="80%">
    <tr bgcolor="#ccccc0" class="left-align">
        <th>Job Name</th>
        <th>Last Run</th>
        <th>Next Run</th>
        <th>Paused</th>
        <th>Run</th>
    </tr>
    <c:forEach var="jobBean" items="${jobs}">
        <tr>
            <td>
                    ${jobBean.name}
            </td>
            <td>
                <c:choose>
                    <c:when test="${empty jobBean.lastExecution}">
                        Never
                    </c:when>
                    <c:otherwise>
                        <fmt:formatDate value="${jobBean.lastExecution}" type="both" timeStyle="short"
                                        dateStyle="short"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${empty jobBean.nextExecution}">
                        Never
                    </c:when>
                    <c:otherwise>
                        <fmt:formatDate value="${jobBean.nextExecution}" type="both" timeStyle="short"
                                        dateStyle="short"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                    ${jobBean.paused ? "YES": "NO"}
            </td>
            <td>
                <a href="?action=<%=QuartzJobsBean.Action.RUN%>&job=${jobBean.name}&requestID=${requestID}">Run</a>
                <c:choose>
                    <c:when test="${jobBean.paused}">
                        <a href="?action=<%=QuartzJobsBean.Action.RESUME%>&job=${jobBean.name}&requestID=${requestID}">Resume</a>
                    </c:when>
                    <c:otherwise>
                        <a href="?action=<%=QuartzJobsBean.Action.PAUSE%>&job=${jobBean.name}&requestID=${requestID}">Pause</a>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
</table>
