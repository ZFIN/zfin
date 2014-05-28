<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.framework.presentation.UserRequestTrackBean" scope="request"/>

<c:set var="clickstream" value="${formBean.clickstream}"/>

<p><a href="/action/dev-tools/view-user-request-tracks">Back to all streams</a></p>


<h3>User Requests for: ${zfn:getPerson(clickstream.session)}</h3>

<table>
    <tr class="search-result-table-entries">
        <td class="bold" width="150"> Initial Referrer</td>
        <td><a href="${clickstream.initialReferrer}">${clickstream.initialReferrer}</a></td>
    </tr>
    <tr>
        <td class="bold"> Hostname</td>
        <td> ${clickstream.hostname}</td>
    </tr>
    <tr class="search-result-table-entries">
        <td class="bold"> Session ID</td>
        <td> ${formBean.sid}</td>
    </tr>
    <tr>
        <td class="bold"> Bot</td>
        <td> ${clickstream.bot}</td>
    </tr>
    <tr class="search-result-table-entries">
        <td class="bold"> Stream Start</td>
        <td><fmt:formatDate value="${clickstream.start}" pattern="yyyy/MM/dd hh:mm:ss"/></td>
    </tr>
    <tr>
        <td class="bold"> Last Request</td>
        <td><fmt:formatDate value="${clickstream.lastRequest}" pattern="yyyy/MM/dd hh:mm:ss"/></td>
    </tr>
    <tr class="search-result-table-entries">
        <td class="bold"> Session Length</td>
        <td> ${zfn:getTimeDuration(clickstream.start, clickstream.lastRequest)} </td>
    </tr>
    <tr>
        <td class="bold"> Session Idle</td>
        <td> ${zfn:getTimeDuration(clickstream.lastRequest, null)} </td>
    </tr>
    <tr class="search-result-table-entries">
        <td class="bold"> # of Requests</td>
        <td> ${fn:length(clickstream.stream)} </td>
    </tr>
    <c:if test="${formBean.time > 0}">
        <tr>
            <td class="bold"> Specific Request Time</td>
            <td>
            <fmt:formatDate value="${formBean.specificTimeOfRequest}" pattern="MMM d, yyyy"/> &nbsp;
            <fmt:formatDate value="${formBean.specificTimeOfRequest}" pattern="hh:mm:ss"/>
            </td>
        </tr>
    </c:if>
</table>

<h3>Click Stream:</h3>
<table class="search groupstripes staticcontent">
    <tbody>
    <tr class="search-result-table-header">
        <th width="35">ID</th>
        <th style="display:none">Date</th>
        <th>Access Time</th>
        <th>Idle time between Requests</th>
        <th>URL</th>
    </tr>
    <c:forEach var="singleStream" items="${clickstream.stream}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${clickstream.stream}" groupByBean="timestamp">
            <td> ${loop.index+1}</td>
            <td style="display:none;">
                <fmt:formatDate value="${singleStream.timestamp}" pattern="MM/dd/yyyy"/>
            </td>
            <td>
                <span title="<fmt:formatDate value="${singleStream.timestamp}" pattern="MMM d, yyyy hh:mm:ss,SSS"/>">
                <fmt:formatDate value="${singleStream.timestamp}" pattern="hh:mm:ss"/>
                    </span>
            </td>
            <td>
                ${zfn:getTimeBetweenRequests(clickstream.stream, loop.index)}
            </td>
            <c:choose>
                <c:when test="${loop.index eq formBean.indexOfRequest}">
                    <td style="background-color:#ff8c00;">
                </c:when>
                <c:otherwise>
                    <td>
                </c:otherwise>
            </c:choose>
            <a href="http://${singleStream}">${singleStream}</a>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
    </tbody>
</table>

<hr style="background-color:green; width:100%"/>

<span class="bold">Search:</span>
<form:form commandName="formBean" method="GET">
    URL contains:
    <form:input path="urlSearchString"/>
    <input type="submit"/>
</form:form>

<c:if test="${!empty formBean.urlSearchString}">
    Searching for [${formBean.urlSearchString}].
</c:if>
