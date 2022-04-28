<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="person" class="org.zfin.profile.Person" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="BIOGRAPHY" value="Biography and Research Interest"/>
<c:set var="CITATIONS" value="Publications"/>
<c:set var="NON_ZFIN_CITATIONS" value="Non-Zebrafish Publications"/>

<c:set var="secs"
       value="${[SUMMARY, BIOGRAPHY, CITATIONS, NON_ZFIN_CITATIONS]}"/>

<z:dataPage sections="${secs}">

    <jsp:attribute name="entityName">
                ${person.fullName}
    </jsp:attribute>

    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
            <nav class="navbar navbar-light admin text-center border-bottom">
                <a class="col-sm" href="/action/profile/person/view/${person.zdbID}">Old View</a>
                <a class="col-sm" href="/action/profile/person/edit/${person.zdbID}">Edit</a>
                <a href="javascript:" class="root" onclick="location.replace('/action/infrastructure/deleteRecord/${person.zdbID}');">Delete</a>
                <a class="col-sm" href='/action/profile/lab/all-labs'>All labs</a>
                <a class="col-sm" href='/action/profile/company/all-companies'>All companies</a>
                <a class="col-sm" href='/action/profile/person/all-people/A'>All people</a>
                <a class="col-sm" href="/action/updates/${person.zdbID}">
                    Last Update:
                    <c:set var="latestUpdate" value="${zfn:getLastUpdate(person.zdbID)}"/>
                    <c:choose>
                <c:when test="${!empty latestUpdate}">
                    <fmt:formatDate value="${latestUpdate.dateUpdated}" type="date"/>
                </c:when>
                <c:otherwise>
                    Never modified
                </c:otherwise>
            </c:choose>
                </a>
            </nav>
        </authz:authorize>
        <authz:authorize access="hasRole('submit')">
                        <c:if test="${isOwner}">
                            <nav class="navbar navbar-light admin text-center border-bottom">
                                <a class="col-sm" href="/action/profile/person/edit/${person.zdbID}">Edit</a>
                            </nav>
                        </c:if>
       </authz:authorize>
    </jsp:attribute>

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">Person</div>
            <h1>${person.fullName}
                <c:if test="${person.deceased}"> (Deceased) </c:if>
            </h1>
            <jsp:include page="person-view-summary.jsp"/>
        </div>

        <z:section title="${BIOGRAPHY}">
            <div id='bio'>
                <zfin2:splitLines input="${person.personalBio}"/>
            </div>

        </z:section>

        <z:section title="${CITATIONS}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${person.zdbID}"></div>
        </z:section>

        <z:section title="${NON_ZFIN_CITATIONS}">
            <zfin2:splitLines input="${person.nonZfinPublications}"/>
        </z:section>
    </jsp:body>

</z:dataPage>
