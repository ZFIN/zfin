<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<h3>Redundancy Runs</h3>
<table class="searchresults" width="100%">
    <tr style="background: #ccc">
        <th width="25%">Name/Source</th>
        <th width="15%">ToDo</th>
        <th width="20%">Date</th>
        <th width="20%">Program</th>
        <th width="20%">Database</th>
    </tr>
    <c:forEach var="run" items="${formBean.redundancyRuns}" varStatus="loop">

        <zfin:alternating-tr loopName="loop">
            <td width="25%">
                <a href="/action/reno/candidate/inqueue/<c:out value="${run.zdbID}"/>">${run.name}</a>
            </td>

            <td width="15%">${run.queueCandidateCount}</td>
            <td width="20%">${run.date}</td>
            <td width="20%">${run.program}</td>
            <td width="20%">${run.blastDatabase}</td>
        </zfin:alternating-tr>
        </c:forEach>
</table>

<p></p>

<h3>Nomenclature Runs</h3>
<table class="searchresults" width="100%">
    <tr style="background: #ccc">
        <th width="25%">Name/Source</th>
        <th width="15%">ToDo</th>
        <th width="20%">Date</th>
        <th width="20%">Program</th>
        <th width="20%">Database</th>
    </tr>
    <c:forEach var="run" items="${formBean.nomenclatureRuns}" varStatus="loop">

        <c:choose>
            <c:when test="${loop.count % 2 == 0}"> <tr class="odd"></c:when>
            <c:otherwise> <tr></c:otherwise>
        </c:choose>

        <td width="25%">
            <a href="/action/reno/candidate/inqueue/<c:out value="${run.zdbID}"/>">${run.name}</a>
        </td>

        <td width="15%">${run.queueCandidateCount}</td>
        <td width="20%">${run.date}</td>
        <td width="20%">${run.program}</td>
        <td width="20%">${run.blastDatabase}</td>

        </tr>
    </c:forEach>
</table>
