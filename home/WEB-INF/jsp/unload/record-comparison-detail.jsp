<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table>
    <tr>
        <td>${previousDate}</td>
        <td><c:out value="${previousRecordLine}" escapeXml="true"/></td>
    </tr>
    <tr>
        <td>${currentDate}</td>
        <td><c:out value="${recordLine}" escapeXml="true"/></td>
    </tr>
</table>
