<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<h1>PUB METRICS</h1>

<table>
    <c:forEach items="${results}" var="result">
        <tr>
            <td>${result.statusId}</td>
            <td>${result.locationId}</td>
            <td>${result.date}</td>
            <td>${result.count}</td>
        </tr>
    </c:forEach>
</table>