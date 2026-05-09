<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="OVERVIEW"           value="Overview"/>
<c:set var="ACCEPTANCE_REASONS" value="Acceptance Reasons"/>
<c:set var="LINKED_FEATURES"    value="Linked Features"/>
<c:set var="BACKGROUND"         value="Background"/>
<c:set var="ADDITIONAL"         value="Additional Info"/>

<c:set var="sections" value="${[OVERVIEW, ACCEPTANCE_REASONS, LINKED_FEATURES, BACKGROUND, ADDITIONAL]}"/>

<c:set var="isNewSubmission" value="${empty submission.zdbID}"/>
<c:set var="entityLabel" value="${not empty submission.name
    ? submission.name
    : (isNewSubmission ? 'New Line Submission' : submission.zdbID)}"/>
<c:choose>
    <c:when test="${isNewSubmission}"><c:set var="pageTitle" value="New Line Submission"/></c:when>
    <c:otherwise><c:set var="pageTitle" value="Edit Line Submission: ${entityLabel}"/></c:otherwise>
</c:choose>

<z:dataPage sections="${sections}" title="${pageTitle}">

    <jsp:attribute name="entityName">${entityLabel}</jsp:attribute>

    <jsp:attribute name="pageBar">
        <nav class="navbar navbar-light admin text-center border-bottom">
            <a class="col-sm" href="/action/zirc/dashboard">Dashboard</a>
            <c:choose>
                <c:when test="${isNewSubmission}">
                    <span class="col-sm">Detail</span>
                </c:when>
                <c:otherwise>
                    <a class="col-sm" href="/action/zirc/line-submission/${submission.zdbID}">Detail</a>
                </c:otherwise>
            </c:choose>
            <span class="col-sm">Edit</span>
        </nav>
    </jsp:attribute>

    <jsp:body>

        <div class="small text-uppercase text-muted">${isNewSubmission ? 'New Line Submission' : 'Edit Line Submission'}</div>
        <h1>${entityLabel}</h1>

        <%-- Single mount; the React component renders the section <section> blocks
             with anchor IDs that match the dataPage side nav. data-submission-id
             is empty for /new — the component creates the row on first save. --%>
        <div class="__react-root" id="LineSubmissionEdit"
             data-submission-id="${not empty submission.zdbID ? submission.zdbID : ''}"></div>


    </jsp:body>
</z:dataPage>
