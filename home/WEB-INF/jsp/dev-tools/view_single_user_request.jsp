<%@ page import="java.util.*,
                 com.opensymphony.clickstream.Clickstream,
                 com.opensymphony.clickstream.ClickstreamRequest" %>
<%
    if (request.getParameter("sid") == null) {
        response.sendRedirect("clickstreams.jsp");
        return;
    }

    Map clickstreams = (Map) application.getAttribute("clickstreams");

    Clickstream stream = null;

    if (clickstreams.get(request.getParameter("sid")) != null) {
        stream = (Clickstream) clickstreams.get(request.getParameter("sid"));
    }

    if (stream == null) {
        response.sendRedirect("clickstreams.jsp");
        return;
    }
%>
<p align="right"><a href="/action/dev-tools/view-user-request-tracks">All streams</a></p>

<h3>Clickstream for <%= stream.getHostname() %></h3>

<b>Initial Referrer</b>: <a href="<%= stream.getInitialReferrer() %>"><%= stream.getInitialReferrer() %></a><br>
<b>Hostname</b>: <%= stream.getHostname() %><br>
<b>Session ID</b>: <%= request.getParameter("sid") %><br>
<b>Bot</b>: <%= stream.isBot() ? "Yes" : "No" %><br>
<b>Stream Start</b>: <%= stream.getStart() %><br>
<b>Last Request</b>: <%= stream.getLastRequest() %><br>

<% long streamLength = stream.getLastRequest().getTime() - stream.getStart().getTime(); %>
<b>Session Length</b>:
<%= (streamLength > 3600000 ?
        " " + (streamLength / 3600000) + " hours" : "") +
        (streamLength > 60000 ?
                " " + ((streamLength / 60000) % 60) + " minutes" : "") +
        (streamLength > 1000 ?
                " " + ((streamLength / 1000) % 60) + " seconds" : "") %><br>

<b># of Requests</b>: <%= stream.getStream().size() %>

<h3>Click stream:</h3>

<ol>
    <%
        synchronized (stream) {
            Iterator clickstreamIt = stream.getStream().iterator();

            while (clickstreamIt.hasNext()) {
                String click = ((ClickstreamRequest) clickstreamIt.next()).toString();
    %>
    <li><a href="http://<%= click %>"><%= click %></a></li>
    <%
            }
        }
    %>
</table>
