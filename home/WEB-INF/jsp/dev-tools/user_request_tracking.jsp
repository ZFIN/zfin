<%@ page import="java.util.*,
                 com.opensymphony.clickstream.Clickstream" %>
<%@ page import="org.acegisecurity.context.SecurityContext" %>
<%@ page import="org.zfin.people.Person" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<jsp:useBean id="formBean" class="org.zfin.framework.presentation.UserRequestTrackBean" scope="request"/>
<%
    final Map clickstreams = (Map) application.getAttribute("clickstreams");

    String showbots = "false";
    if ("true".equalsIgnoreCase(request.getParameter("showbots")))
        showbots = "true";
    else if ("both".equalsIgnoreCase(request.getParameter("showbots")))
        showbots = "both";
%>
<h3>Active Clickstreams</h3>

<form:form commandName="formBean" method="get">
    <form:input path="searchString"/>
    <input type="submit"/>
</form:form>

<c:if test="${!empty formBean.searchString}">
    Searching for [${formBean.searchString}].
</c:if>

<p>
    <a href="?showbots=false">User Streams</a> |
    <a href="?showbots=true">Bot Streams</a> |
    <a href="?showbots=both">Both</a>
</p>

<% if (clickstreams.isEmpty()) { %>
<p>No clickstreams in progress.</p>
<% } else { %>
<ol>
    <%

        synchronized (clickstreams) {
            Iterator it = clickstreams.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                Clickstream stream = (Clickstream) clickstreams.get(key);

                if (showbots.equals("false") && stream.isBot()) {
                    continue;
                } else if (showbots.equals("true") && !stream.isBot()) {
                    continue;
                }

                try {
    %>
    <li>
        <%--<ul>--%>
        <%--ACEGI_SECURITY_LAST_USERNAME--%>
        <%--ACEGI_SECURITY_CONTEXT--%>
        <%
            HttpSession sessionStream = stream.getSession();
            Object name = sessionStream.getAttribute("ACEGI_SECURITY_LAST_USERNAME");
            SecurityContext securityContext = (SecurityContext)sessionStream.getAttribute("ACEGI_SECURITY_CONTEXT");
        %>
        <a href="/action/dev-tools/view-single-user-request-tracking?sid=<%= key %>"
                ><%= (name==null ? "Guest" : name) %></a>
        <small>
            <%
                if(name!=null){
                    out.println("("+((Person)securityContext.getAuthentication().getPrincipal()).getFullName() +")");
                }
            %>
            [
            <c:if test="${!empty formBean.searchString}">
                <%
                    int count = 0 ;
                    for(Iterator iterator = stream.getStream().iterator() ; iterator.hasNext(); ){
                        if(iterator.next().toString().toLowerCase().contains(formBean.getSearchString())) {
                            ++count ;
                        }
                    }
                    if(count>0){
                        out.println("<font color=red>"+count+" matches</font>");
                    }
                %>
            </c:if>


            <%= stream.getStream().size() %> reqs]


            <%
                out.println("from[" + stream.getHostname()+"]") ;
                int timeDif = (int) (stream.getLastRequest().getTime() - stream.getStart().getTime()) / (1000*60);
                out.println(stream.getStart() + " to " + stream.getLastRequest()  + ": " + timeDif + " (min)") ;
                // should never be null
                out.println("session [" + sessionStream.getId()+"]") ;
            %>
        </small></li>
    <%
    }
    catch (Exception e) {
    %>
    An error occurred - <%= e %><br>
    <%
                    }
                }
            }
        }
    %>
</ol>
