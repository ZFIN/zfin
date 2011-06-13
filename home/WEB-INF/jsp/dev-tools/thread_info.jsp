<jsp:useBean id="threadMXBean" scope="request" type="java.lang.management.ThreadMXBean"/>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Thread Summary</span></th>
    </tr>
</table>

<div class="summary">
    <div class="summaryTitle">Number of Threads: ${threadMXBean.threadCount}
    <div class="summaryTitle"> Current Thread: <%= Thread.currentThread().getName()%></div>
    <div class="summaryTitle"> Deadlocked Threads: ${deadlockedThreads}</div>
    <div class="summaryTitle"> Monitor Deadlocked Threads: ${monitorDeadlockedThreads}</div>
    </div>
</div>


<table class="summary">
    <tr>
        <th>Id</th>
        <th>Thread Id</th>
        <th>Name</th>
        <th>State</th>
        <th>Suspended</th>
    </tr>
    <c:forEach var="thread" items="${allThreads}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>${loop.index}</td>
            <td>${thread.threadId}</td>
            <td><a href="single-thread-info?threadID=${thread.threadId}"> ${thread.threadName}</a>
                <a href="single-thread-info?threadID=${thread.threadId}" class="popup-link data-popup-link"/>
                ${zfn:lastZfinCall(thread.threadId)}
            </td>
            <td>${thread.threadState}</td>
            <td>${thread.suspended}</td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

    <div class="summaryTitle"> Total Thread count started: ${threadMXBean.totalStartedThreadCount}
    <div class="summaryTitle"> Peak Thread count: ${threadMXBean.peakThreadCount}
