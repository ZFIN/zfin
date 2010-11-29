<%@ page import="org.zfin.people.Person" %>
<%@ page import="org.zfin.framework.presentation.UserRequestTrackBean" %>
<%@ page import="org.zfin.framework.presentation.UserRequestTrackController" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.framework.presentation.UserRequestTrackBean" scope="request"/>

<hr style="background-color:green; width:100%"/>

<span class="bold">Active Clickstreams</span>
<c:if test="${!empty formBean.urlSearchString}">
    <span class="red">
    that have [${formBean.urlSearchString}] in any URL request.
    </span>

    <p/>
    <a href="view-user-request-tracks">Show All</a>
</c:if>
<c:if test="${!empty formBean.showBots}">
    <span class="red">
        <c:if test="${formBean.showBots eq 'TRUE'}">
            Robot requests only
        </c:if>
        <c:if test="${formBean.showBots eq 'FALSE'}">
            Excluding Robot requests
        </c:if>
    </span>

    <p/>
    <a href="view-user-request-tracks">Show All</a>
</c:if>

<c:choose>
    <c:when test="${formBean.clickStreams == null or fn:length(formBean.clickStreams) == 0 }">
        <p>No clickstreams in progress.</p>
    </c:when>
    <c:otherwise>
        <table class="search groupstripes staticcontent">
            <tbody>
            <tr bgcolor="#ccccc0">
                <th width="45">ID</th>
                <th>Owner</th>
                <th>Requests</th>
                <th>Start Time
                </th>
                <th>Time Last Accessed
                </th>
                <th>Active Session Duration</th>
                <th>Session Idle</th>
                <th>Session ID</th>
                <th>Robot</th>
            </tr>
            </tbody>
            <c:forEach var="singleStream" items="${formBean.clickStreams}" varStatus="loop">
                <zfin:alternating-tr loopName="loop" groupByBean="sessionID"
                                     groupBeanCollection="${formBean.clickStreams}">
                    <td>${loop.index+1}</td>
                    <td>
                        <a href="/action/dev-tools/view-single-user-request-tracking?sid=${singleStream.sessionID}">
                                ${zfn:getPerson(singleStream.clickstream.session)}
                        </a>
                    </td>
                    <td width=50>
                            ${fn:length(singleStream.clickstream.stream)}
                    </td>
                    <td width="35">
                        <span title="<fmt:formatDate value="${singleStream.clickstream.start}" pattern="MMM d, yyyy"/>">
                        <fmt:formatDate value="${singleStream.clickstream.start}" pattern="hh:mm:ss"/>
                            </span>
                    </td>
                    <td width="35">
                        <span title="<fmt:formatDate value="${singleStream.clickstream.lastRequest}" pattern="MMM d, yyyy"/>">
                        <fmt:formatDate value="${singleStream.clickstream.lastRequest}" pattern="hh:mm:ss"/>
                           </span>
                    </td>
                    <td>
                            ${zfn:getTimeDuration(singleStream.clickstream.start, singleStream.clickstream.lastRequest)}
                    </td>
                    <td>
                            ${zfn:getTimeDuration(singleStream.clickstream.lastRequest, null)}
                    </td>
                    <td>
                            ${singleStream.sessionID}
                    </td>
                    <td>
                            ${singleStream.clickstream.bot}
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

<hr style="background-color:green; width:100%"/>

<p>
    <a href="?showBots=<%=UserRequestTrackController.ShowBot.FALSE%>">User Streams</a> |
    <a href="?showBots=<%=UserRequestTrackController.ShowBot.TRUE%>">Bot Streams</a> |
    <a href="?showBots=<%=UserRequestTrackController.ShowBot.BOTH%>">Both</a>
</p>

<hr style="background-color:green; width:100%"/>

<span class="bold">Search Clickstream that contains URL request:</span>
<form:form commandName="formBean" method="GET">
    URL contains:
    <form:input path="urlSearchString"/>
    <input type="submit" name="Search"/>
</form:form>


