<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="OVERVIEW"           value="Overview"/>
<c:set var="MUTATIONS"          value="Mutations"/>
<c:set var="LINKED_FEATURES"    value="Linked Features"/>
<c:set var="BACKGROUND"         value="Background"/>
<c:set var="ADDITIONAL"         value="Additional Info"/>

<c:set var="sections" value="${[OVERVIEW, MUTATIONS, LINKED_FEATURES, BACKGROUND, ADDITIONAL]}"/>

<c:set var="entityLabel" value="${not empty submission.name ? submission.name : submission.zdbID}"/>

<z:dataPage sections="${sections}" subSections="${subSections}" subSectionStatus="${subSectionStatus}" subSubSections="${subSubSections}" subSubSectionStatus="${subSubSectionStatus}" title="Line Submission: ${entityLabel}">

    <jsp:attribute name="entityName">${entityLabel}</jsp:attribute>

    <jsp:attribute name="pageBar">
        <nav class="navbar navbar-light admin text-center border-bottom">
            <a class="col-sm" href="/action/zirc/dashboard">Dashboard</a>
            <span class="col-sm">Detail</span>
            <a class="col-sm" href="/action/zirc/line-submission/${submission.zdbID}/edit">Edit</a>
        </nav>
    </jsp:attribute>

    <jsp:body>

        <div class="small text-uppercase text-muted">ZIRC Line Submission</div>
        <h1><c:out value="${entityLabel}"/></h1>

        <%-- Schema-driven detail view. The React component reads the status /
             audit payload from the script block below; data shape is curated
             server-side so the existing LineSubmissionStatusComputer / Updates
             pipeline keeps working unchanged. --%>
        <script type="application/json" id="ls-detail-status-payload">${statusPayloadJson}</script>

        <div class="__react-root" id="LineSubmissionDetail"
             data-submission-id="${submission.zdbID}"></div>

    </jsp:body>
</z:dataPage>
