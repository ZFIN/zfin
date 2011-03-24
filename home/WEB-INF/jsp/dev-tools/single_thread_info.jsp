<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="thread" scope="request" type="java.lang.management.ThreadInfo"/>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Thread&nbsp;Name:</span></th>
        <td><span class="name-value">${thread.threadName} (${thread.threadState})</span></td>
    </tr>
</table>

<a href="thread-info">Back to Thread Summary</a>

<div class="summary">
    <span class="summaryTitle">Locked Object</span>
    ${thread.lockName}
</div>

<div class="summary">
    <span class="summaryTitle">Lock owner ID</span>
    ${thread.lockOwnerName} ( ${thread.lockOwnerId})
</div>

<div class="summary">
    <span class="summaryTitle">Suspended</span>
    ${thread.suspended}
</div>

<div class="summary">
    <span class="summaryTitle">Running native code</span>
    ${thread.inNative}
</div>

<div class="summary">
    <span class="summaryTitle">Waited count</span>
    ${thread.waitedCount} (${thread.waitedTime}ms)
</div>

<div class="summary">
    <span class="summaryTitle">Monitors this thread is locking</span>
    <c:forEach var="monitor" items="${thread.lockedMonitors}">
        ${monitor.className}
    </c:forEach>
</div>

<div class="summary">
    <div class="summaryTitle">Stack Trace</div>
    <div style="text-indent:20pt">
        <table>
            <c:forEach var="element" items="${thread.stackTrace}">
                <tr>
                    <td style="text-indent:20pt">
                            ${element}
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>

