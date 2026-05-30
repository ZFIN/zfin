<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="ACTIVE" value="Active Line Submissions"/>
<c:set var="CLOSED" value="Closed Line Submissions"/>

<c:set var="sections" value="${[ACTIVE, CLOSED]}"/>

<z:dataPage sections="${sections}" title="ZIRC Line Submission Dashboard">

    <jsp:attribute name="entityName">Line Submission Dashboard</jsp:attribute>

    <jsp:body>

        <div class="small text-uppercase text-muted">ZIRC</div>
        <h1><img src="/images/zirc_logo20061006.gif" alt="ZIRC" class="ml-2" style="height:1em;vertical-align:baseline"> Line Submission Dashboard </h1>

        <p>
            <a href="/action/zirc/line-submission/new" class="btn btn-primary">
                <i class="fas fa-plus-circle mr-1"></i>Start a new line submission
            </a>
        </p>

        <z:section title="${ACTIVE}">
            <table class="table table-striped w-auto">
                <thead>
                    <tr>
                        <th>Line Submission Name</th>
                        <th>Date Started</th>
                        <th>Status</th>
                        <th>Submitter(s)</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty activeSubmissions}">
                            <tr>
                                <td colspan="4" class="text-muted">No active submissions.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${activeSubmissions}" var="sub" varStatus="loop">
                                <tr>
                                    <td><a href="/action/zirc/line-submission/${sub.zdbID}/detail-react">${sub.name}</a></td>
                                    <td><fmt:formatDate value="${sub.createdAt}" pattern="yyyy-MM-dd"/></td>
                                    <td><z:zirc-status-badge status="${overallStatus[sub.zdbID]}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty sub.persons}">&mdash;</c:when>
                                            <c:otherwise><c:forEach items="${sub.persons}" var="lsp" varStatus="ploop"><a href="/action/profile/person/view/${lsp.person.zdbID}"><c:if test="${not empty lsp.person.firstName}">${fn:substring(lsp.person.firstName, 0, 1)}. </c:if><c:out value="${lsp.person.lastName}"/></a><c:if test="${!ploop.last}">, </c:if></c:forEach></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </z:section>

        <z:section title="${CLOSED}">
            <table class="table table-striped w-auto">
                <thead>
                    <tr>
                        <th>Line Submission Name</th>
                        <th>Date Started</th>
                        <th>Status</th>
                        <th>Submitter(s)</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty closedSubmissions}">
                            <tr>
                                <td colspan="4" class="text-muted">No closed submissions.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${closedSubmissions}" var="sub" varStatus="loop">
                                <tr>
                                    <td><a href="/action/zirc/line-submission/${sub.zdbID}/detail-react">${sub.name}</a></td>
                                    <td><fmt:formatDate value="${sub.createdAt}" pattern="yyyy-MM-dd"/></td>
                                    <td><z:zirc-status-badge status="${overallStatus[sub.zdbID]}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty sub.persons}">&mdash;</c:when>
                                            <c:otherwise><c:forEach items="${sub.persons}" var="lsp" varStatus="ploop"><a href="/action/profile/person/view/${lsp.person.zdbID}"><c:if test="${not empty lsp.person.firstName}">${fn:substring(lsp.person.firstName, 0, 1)}. </c:if><c:out value="${lsp.person.lastName}"/></a><c:if test="${!ploop.last}">, </c:if></c:forEach></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </z:section>

    </jsp:body>
</z:dataPage>
