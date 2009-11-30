<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<strong>Quartz Job Scheduler: </strong>
<c:choose>
    <c:when test="${formBean.jobsRunning}">
        Running
        <a href="?action=pauseAll">Pause</a>
    </c:when>
    <c:otherwise>
        <a href="?action=resumeAll">Start</a>
        Paused
    </c:otherwise>
</c:choose>
<br>
<br>


<c:if test="${!empty formBean.message}">
    <b>Last Executed:</b> ${formBean.message}
</c:if>

<c:if test="${!empty formBean.action}">
    <b>Action:</b> ${formBean.action}
    <br>
    <b>Job:</b> ${formBean.job}
    <br>
    <br>
</c:if>

<br>

<a href="/action/dev-tools/quartz-jobs">[refresh]</a>


<h3>Quartz Jobs</h3>


<table>
    <tr>
        <th>Job Name</th>
        <th>Last Run</th>
        <th>Next Run</th>
        <th>Paused</th>
        <th>Run</th>
    </tr>
    <c:forEach var="jobBean" items="${formBean.quartzJobInfoList}">
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
                <a href="?action=run&job=${jobBean.name}">Run</a>
                <c:choose>
                    <c:when test="${jobBean.paused}">
                        <a href="?action=resume&job=${jobBean.name}">Resume</a>
                    </c:when>
                    <c:otherwise>
                        <a href="?action=pause&job=${jobBean.name}">Pause</a>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
</table>
