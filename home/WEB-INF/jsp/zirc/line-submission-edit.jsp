<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- New line submissions only. Existing ones are edited on the submission page
     (line-submission-detail-react.jsp); /line-submission/{id}/edit redirects
     there (ZFIN-10543). After the first save creates the row, the URL switches
     to that page's. --%>

<c:set var="OVERVIEW"           value="Overview"/>
<c:set var="MUTATIONS"          value="Mutations"/>
<c:set var="LINKED_FEATURES"    value="Linked Features"/>
<c:set var="BACKGROUND"         value="Background"/>
<c:set var="ADDITIONAL"         value="Additional Info"/>

<c:set var="sections" value="${[OVERVIEW, MUTATIONS, LINKED_FEATURES, BACKGROUND, ADDITIONAL]}"/>

<z:dataPage sections="${sections}" title="New Line Submission">

    <jsp:attribute name="entityName">New Line Submission</jsp:attribute>

    <jsp:attribute name="pageBar">
        <nav class="navbar navbar-light admin text-center border-bottom">
            <a class="col-sm" href="/action/zirc/dashboard">Dashboard</a>
            <span class="col-sm">New Submission</span>
        </nav>
    </jsp:attribute>

    <jsp:body>

        <div class="small text-uppercase text-muted">New Line Submission</div>
        <h1>New Line Submission</h1>

        <%-- Single mount; the React component renders the section <section> blocks
             with anchor IDs that match the dataPage side nav. The component
             creates the row on first save. --%>
        <div class="__react-root" id="LineSubmissionEdit" data-submission-id=""></div>

    </jsp:body>
</z:dataPage>
