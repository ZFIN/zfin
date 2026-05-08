<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="OVERVIEW"   value="Overview"/>
<c:set var="BACKGROUND" value="Background"/>
<c:set var="ADDITIONAL" value="Additional Info"/>

<c:set var="sections" value="${[OVERVIEW, BACKGROUND, ADDITIONAL]}"/>

<c:set var="entityLabel" value="${not empty submission.name ? submission.name : submission.zdbID}"/>

<z:dataPage sections="${sections}" title="Edit Line Submission: ${entityLabel}">

    <jsp:attribute name="entityName">${entityLabel}</jsp:attribute>

    <jsp:body>

<%--        TODO: this should be styled in the same way other similar nav bars are (think the edit pub page)--%>
        <p>
            <a href="/action/zirc/line-submission/${submission.zdbID}" class="btn btn-light btn-sm">&laquo; View detail</a>
            <a href="/action/zirc/dashboard" class="btn btn-light btn-sm">Back to Dashboard</a>
        </p>

        <div class="small text-uppercase text-muted">Edit Line Submission</div>
        <h1>${entityLabel}</h1>

        <%-- Single mount; the React component renders all three <section> blocks
             with anchor IDs that match the dataPage side nav. --%>
        <div class="__react-root" id="LineSubmissionEdit"
             data-submission-id="${submission.zdbID}"></div>


    </jsp:body>
</z:dataPage>
