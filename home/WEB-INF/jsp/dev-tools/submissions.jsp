<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Form Submissions">
    <div class="container-fluid" style="margin: 20px;">
        <h2>Public Form Submissions</h2>
        <p class="text-muted">
            Every submission through the public forms, including the ones discarded without telling
            the submitter. A real request wrongly scored as spam can only be found here.
        </p>

        <form method="get" class="form-inline mb-3">
            <label for="outcome" class="mr-2">Outcome</label>
            <select name="outcome" id="outcome" class="form-control mr-2">
                <option value="">All</option>
                <c:forEach var="outcome" items="${outcomes}">
                    <option value="${outcome}" ${outcome eq selectedOutcome ? 'selected' : ''}>${outcome}</option>
                </c:forEach>
            </select>
            <label for="limit" class="mr-2">Rows</label>
            <input type="number" name="limit" id="limit" class="form-control mr-2"
                   style="width: 7em" min="1" max="${maxLimit}" value="${limit}">
            <input type="submit" class="btn btn-primary" value="Filter">
        </form>

        <%-- Say outright when older rows exist, so the page is never mistaken for the whole log. --%>
        <p>
            <c:choose>
                <c:when test="${total lt 0}">
                    Showing ${fn:length(submissions)}. The total could not be read; see the log.
                </c:when>
                <c:when test="${fn:length(submissions) lt total}">
                    Showing the newest ${fn:length(submissions)} of ${total}. Raise <em>Rows</em> to
                    see older ones.
                </c:when>
                <c:otherwise>
                    Showing all ${total} matching submissions.
                </c:otherwise>
            </c:choose>
        </p>

        <c:choose>
            <c:when test="${empty submissions}">
                <p>No submissions recorded.</p>
            </c:when>
            <c:otherwise>
                <table class="table table-striped table-sm">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Outcome</th>
                        <th>Score</th>
                        <th>Why</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>ORCID</th>
                        <th>IP</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="submission" items="${submissions}">
                        <tr>
                            <td>${submission.id}</td>
                            <td style="white-space: nowrap">
                                <fmt:formatDate value="${submission.date}" pattern="yyyy-MM-dd HH:mm"/>
                            </td>
                            <td>${submission.type}</td>
                            <td>${submission.outcome}</td>
                            <td>${submission.spamScore}</td>
                            <td>
                                <c:out value="${submission.spamReasons}"/>
                                <c:if test="${not empty submission.validationErrors}">
                                    <div class="text-muted">
                                        <small><c:out value="${submission.validationErrors}"/></small>
                                    </div>
                                </c:if>
                            </td>
                            <td><c:out value="${submission.name}"/></td>
                            <td><c:out value="${submission.email}"/></td>
                            <td><c:out value="${submission.orcid}"/></td>
                            <td><c:out value="${submission.ipAddress}"/></td>
                            <td style="white-space: nowrap">
                                <%-- Recovery path for a discarded request: the same prefill link the
                                     coordinator gets by email, which spam rejections never send. --%>
                                <c:if test="${submission.type eq 'PERSON'}">
                                    <a href="/action/profile/person/create?prefill_from_submission=${submission.id}">Create
                                        account</a>
                                </c:if>
                            </td>
                        </tr>
                        <c:if test="${not empty submission.details}">
                            <tr>
                                <td colspan="11">
                                    <details>
                                        <summary class="text-muted">
                                            <small>Full submission</small>
                                        </summary>
                                        <pre style="white-space: pre-wrap"><c:out value="${submission.details}"/></pre>
                                        <div class="text-muted">
                                            <small>User agent: <c:out value="${submission.userAgent}"/></small>
                                        </div>
                                    </details>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</z:devtoolsPage>
