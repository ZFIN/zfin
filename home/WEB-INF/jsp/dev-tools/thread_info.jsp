<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table border="1">
    Current Thread: <%= Thread.currentThread().getName()%> <BR>
    <tr>
        <td class="item-bold">No</td>
        <td class="item-bold"> Thread Group Name</td>
        <td class="item-bold">Thread Name</td>
        <td class="item-bold">Is Alive</td>
        <td class="item-bold">Priority</td>
        <td class="item-bold">Is Daemon</td>
        <td class="item-bold">Is Interrupted</td>
        <td class="item-bold">Stop</td>
    </tr>

    <c:forEach var="thread" items="${threads}" varStatus="position">
        <tr>
        <tr>
            <td class="item"><c:out value="${position.count}" /></td>
            <td class="item"><c:out value="${thread.threadGroup.name}" /></td>
            <td class="item"><c:out value="${thread.name}" /></td>
            <td class="item"><c:out value="${thread.alive}" /></td>
            <td class="item"><c:out value="${thread.priority}" /></td>
            <td class="item"><c:out value="${thread.daemon}" /></td>
            <td class="item"><c:out value="${thread.interrupted}" /></td>
            <td class="item"><a href="/spring/thread-info?groupName=&threadName=">stop</a></td>
        </tr>
    </c:forEach>
</table>
