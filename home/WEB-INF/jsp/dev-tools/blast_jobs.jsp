<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<h3>Blast Jobs</h3>

<c:choose>
    <c:when test="${empty formBean.blastJobs}">
        No jobs in queue.
    </c:when>
    <c:otherwise>

        ${fn:length(formBean.blastJobs)} total jobs.
        <br>
        ${formBean.numJobsRunning} jobs / ${formBean.numThreadsRunning} threads running.
        <br>
        ${formBean.numJobsQueued} jobs / ${formBean.numThreadsQueued} threads queued.
        <br>
        <br>
        <table border="1">
            <tr>
                <th>Job Name</th>
                <th>State</th>
                <th>Number Threads</th>
            </tr>
            <c:forEach var="jobBean" items="${formBean.blastJobs}">
                <tr>
                    <td>
                            ${jobBean.xmlBlastBean.ticketNumber}
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${jobBean.running}">
                                <font color="red">RUNNING</font>
                            </c:when>
                            <c:when test="${!jobBean.running && jobBean.finished}">
                                finished
                            </c:when>
                            <c:when test="${!jobBean.running && !jobBean.finished}">
                                <font color="green">queued</font>
                            </c:when>
                        </c:choose>
                    </td>
                    <td>
                            ${jobBean.numberThreads}
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>
