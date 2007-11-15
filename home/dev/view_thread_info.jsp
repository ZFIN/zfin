<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="java.util.List,
                 java.util.ArrayList,
                 java.util.Arrays,
                 java.util.Comparator"%>
<table border = "1">
            Current Thread: <bean:write name="threadForm" property="currentThread.name" /> <BR>
            Total Number of Threads: <bean:write name="threadForm" property="threadCount" />
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

           <% int position = 0; %>
            <logic:iterate id="thread" name="threadForm" property="allThreads" type="java.lang.Thread" >
            <tr>
                    <% position++; %>
                      <tr>
                      <td class="item"><%= position %></td>
                      <td class="item"><%= thread.getThreadGroup().getName()%></td>
                      <td class="item"><%= thread.getName()%></td>
                      <td class="item"><%= thread.isAlive()%></td>
                      <td class="item"><%= thread.getPriority()%></td>
                      <td class="item"><%= thread.isDaemon()%> </td>
                      <td class="item"><%= thread.isInterrupted()%> </td>
                      <td class="item"><a href="view_thread_info.jsp?groupName=<%= thread.getThreadGroup().getName()%>&threadName=<%= thread.getName()%>">stop</a> </td>
               </tr>
            </logic:iterate>

</table>
