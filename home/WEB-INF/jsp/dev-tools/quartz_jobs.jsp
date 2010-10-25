<%@ page import="org.zfin.framework.presentation.QuartzJobsBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.framework.presentation.QuartzJobsBean" scope="request"/>

<style type="text/css">
    h3 {
        font-variant:small-caps;
    }
</style>

<h3>Job Scheduler: </h3>
<c:choose>
    <c:when test="${formBean.jobsRunning}">
        The scheduler is currently running.
        <a href="?action=<%=QuartzJobsBean.Action.PAUSE_ALL%>">Click to Pause</a>
    </c:when>
    <c:otherwise>
        The scheduler is currently paused.  
        <a href="?action=<%=QuartzJobsBean.Action.RESUME_ALL%>">Click to Start</a>
    </c:otherwise>
</c:choose>
<p/>

<c:if test="${!empty formBean.manualJobsList}">
    <h3>Manual Quartz Jobs</h3>
    <zfin2:quartzJobsList jobs="${formBean.manualJobsList}"/>
</c:if>

<c:if test="${!empty formBean.quartzJobInfoList}">
    <h3>Scheduled Quartz Jobs</h3>
    <zfin2:quartzJobsList jobs="${formBean.quartzJobInfoList}"/>
</c:if>



