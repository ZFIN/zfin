<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div style="text-align: center;">
    <c:choose>
        <c:when test="${empty zdbID}">
            <b>Record not found.</b>
        </c:when>
        <c:otherwise>
            <b>Requested ID: <span style="color:red; font-size: large;"> ${zdbID}</span> not found on ZFIN.</b>
        </c:otherwise>
    </c:choose>
</div>