<%@ page import="java.util.TimeZone" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width="100%" border="0" cellpadding="4" cellspacing="0">
    <tr>
        <td class="sectionTitle" colspan="2">JVM Configuration</td>
    </tr>
    <tr>
        <td class="listContent">
            JVM Max Memory Heap
        </td>
        <td class="listContent">
            <%= Runtime.getRuntime().maxMemory()/1024/1024 %> MB
        </td>
    </tr>
    <tr>
        <td class="listContent">
            JVM Total Memory
        </td>
        <td class="listContent">
            <%= Runtime.getRuntime().totalMemory()/1024/1024 %> MB
        </td>
    </tr>
    <tr>
        <td class="listContent">
            Free Memory
        </td>
        <td class="listContent">
            <%= Runtime.getRuntime().freeMemory()/1024/1024 %> MB
        </td>
    </tr>
    <tr>
        <td class="listContent">
            Time Zone Info
        </td>
        <td class="listContent">
            <%= TimeZone.getDefault() %> 
        </td>
    </tr>
    <tr>
        <td class="sectionTitle">Property Key</td>
        <td class="sectionTitle">Property value</td>
    </tr>
    <c:forEach var="session" items="${properties}">
        <tr>
            <td class="listContent">
                <c:out value='${session.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${session.value}'/>
            </td>
        </tr>
    </c:forEach>
</table>
