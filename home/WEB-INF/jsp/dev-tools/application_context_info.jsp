<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" scope="request" type="org.zfin.framework.presentation.ApplicationContextBean"/>
<jsp:useBean id="runtimeMXBean" scope="request" type="java.lang.management.RuntimeMXBean"/>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Web Application Context Information</span></th>
    </tr>
</table>

<p/>

<table class="summary">
    <tr>
        <th>Key</th>
        <th>Value</th>
    </tr>
    <tr>
        <td> Display Name</td>
        <td> ${formBean.applicationContext.displayName} </td>
    </tr>
    <tr>
        <td> Server Start Day</td>
        <td>
            <zfin2:displayDay date="${formBean.startup}"/>
        </td>
    </tr>
    <tr>
        <td> Server Start Time</td>
        <td>
            <fmt:formatDate value="${formBean.startup}" type="Time"/>
        </td>
    </tr>
    <tr>
        <td> Server Name</td>
        <td> ${runtimeMXBean.name} </td>
    </tr>
    <tr>
        <td> Management Spec Version</td>
        <td> ${runtimeMXBean.managementSpecVersion} </td>
    </tr>
</table>

<p/>

<table class="summary">
    <tr>
        <th>JVM Startup Argument</th>
    </tr>
    <c:forEach var="argument" items="${runtimeMXBean.inputArguments}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>${argument}</td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

