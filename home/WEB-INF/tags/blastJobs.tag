<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="blastStatistics" type="org.zfin.sequence.blast.BlastStatistics"%>
<%@ attribute name="blastJobs" type="org.zfin.sequence.blast.presentation.BlastJobsBean"%>

<jsp:useBean id="now" class="java.util.Date" />
<h3>Load Since <fmt:formatDate value="${blastStatistics.startTime}" type="BOTH" timeStyle="SHORT" dateStyle="MEDIUM"/></h3>
Up Time (s):  <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${( now.time-blastStatistics.startTime.time)/1000 }"/>
<br>
Jobs Finished: ${blastStatistics.numJobs}
<br>
Threads Finished: ${blastStatistics.numThreads}
<br>
<br>

<table border="1">
    <tr>
        <th>Type</th>
        <th>Total (s)</th>
        <th>Per Job (s)</th>
        <th>Per Thread (s)</th>
        <th>Per Uptime</th>
    </tr>
    <tr>
        <td>
            Queued
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${blastStatistics.queueTime /  1000}"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${blastStatistics.queueTime / blastStatistics.numJobs/  1000}"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${blastStatistics.queueTime / blastStatistics.numThreads/  1000}"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="5"
                              value="${ blastStatistics.queueTime/( now.time-blastStatistics.startTime.time) }"/>
        </td>
    </tr>
    <tr>
        <td>
            Running
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${blastStatistics.runTime /  1000}"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${blastStatistics.runTime / blastStatistics.numJobs / 1000}"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${blastStatistics.runTime / blastStatistics.numThreads / 1000}"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="5"
                              value="${ blastStatistics.runTime/( now.time-blastStatistics.startTime.time)}"/>
        </td>
    </tr>
    <tr>
        <td>
            Total
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${ (blastStatistics.runTime+blastStatistics.queueTime) / 1000 }"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${ (blastStatistics.runTime+blastStatistics.queueTime) / blastStatistics.numJobs/ 1000 }"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="0"
                              value="${ (blastStatistics.runTime+blastStatistics.queueTime) / blastStatistics.numThreads / 1000 }"/>
        </td>
        <td>
            <fmt:formatNumber type="number" maxFractionDigits="5"
                              value="${ (blastStatistics.runTime+blastStatistics.queueTime) / ( now.time-blastStatistics.startTime.time)}"/>
        </td>
    </tr>
</table>


<h3>Blast Jobs</h3>
<c:choose>
    <c:when test="${empty blastJobs.blastThreadCollection.queue}">
        No jobs in queue.
    </c:when>
    <c:otherwise>

        ${blastJobs.jobCount} total jobs.
        <br>
        ${blastJobs.runningJobCount} jobs / ${blastJobs.runningThreadCount} threads running.
        <br>
        ${blastJobs.queuedJobCount} jobs / ${blastJobs.queuedThreadCount} threads queued.
        <br>
        <br>
        <table border="1">
            <tr>
                <th>Job Name</th>
                <th>State</th>
                <th>Number Threads</th>
                <th>Time To Start (s)</th>
                <th>Run Time (s)</th>
                <th>Finish Time (s)</th>
                <th>Total Time (s)</th>
            </tr>
            <c:forEach var="jobBean" items="${blastJobs.blastThreadCollection.queue}">
                <tr>
                    <td>
                        <a href="/action/blast/blast-view?resultFile=${jobBean.xmlBlastBean.ticketNumber}">
                                ${jobBean.xmlBlastBean.ticketNumber}
                        </a>

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
                    <td>
                        <c:choose>
                            <c:when test="${jobBean.startTime < now}">
                                ${(jobBean.startTime.time - jobBean.queueTime.time)/1000}
                            </c:when>
                            <c:otherwise>
                                ${(now.time - jobBean.queueTime.time)/1000}
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:if test="${jobBean.startTime < now}">
                            ${(now.time - jobBean.startTime.time)/1000}
                        </c:if>
                    </td>
                    <td>
                        <c:if test="${jobBean.finishTime < now}">
                            ${(now.time - jobBean.finishTime.time)/1000}
                        </c:if>
                    </td>
                    <td>
                            ${(now.time - jobBean.queueTime.time)/1000}
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>
