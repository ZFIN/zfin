<%@ page import="org.zfin.framework.presentation.QuartzJobsBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<strong>Quartz Job Scheduler: </strong>
<c:choose>
    <c:when test="${formBean.jobsRunning}">
        Running
        <a href="?action=<%=QuartzJobsBean.Action.PAUSE_ALL%>&requestID=${formBean.newRequestID}">Pause</a>
    </c:when>
    <c:otherwise>
        <a href="?action=<%=QuartzJobsBean.Action.RESUME_ALL%>&requestID=${formBean.newRequestID}">Start</a>
        Paused
    </c:otherwise>
</c:choose>
<br>

requestID=${formBean.requestID}
<br>


<c:if test="${!empty formBean.message}">
    <b>Last Executed:</b> ${formBean.message}
</c:if>

<c:if test="${!empty formBean.action}">
    <b>Action:</b> ${formBean.action}
    <br>
    <b>Job:</b> ${formBean.job}
    <br>
    <b>Group:</b> ${formBean.group}
    <br>
    <br>
</c:if>

<br>

<a href="/action/dev-tools/quartz-jobs">[refresh]</a>


<c:if test="${!empty formBean.manualJobsList}">
    <h3>Manual Quartz Jobs</h3>
    <zfin2:quartzJobsList jobs="${formBean.manualJobsList}" requestID="${formBean.newRequestID}"/>
</c:if>

<c:if test="${!empty formBean.quartzJobInfoList}">
    <h3>Scheduled Quartz Jobs</h3>
    <zfin2:quartzJobsList jobs="${formBean.quartzJobInfoList}" requestID="${formBean.newRequestID}"/>
</c:if>



