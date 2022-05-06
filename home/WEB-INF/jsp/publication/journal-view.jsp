<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="journal" class="org.zfin.publication.Journal" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="PUBLICATION" value="Publications"/>

<c:set var="secs" value="${[SUMMARY, PUBLICATION]}"/>

<z:dataPage sections="${secs}">

    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
                <nav class="navbar navbar-light admin text-center border-bottom">
                    <a class="col-sm" href="/action/updates/${journal.zdbID}">
                        Last Update:
                        <c:set var="latestUpdate" value="${zfn:getLastUpdate(journal.zdbID)}"/>
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
    </jsp:attribute>

    <jsp:attribute name="entityName">
            <div data-toggle="tooltip" data-placement="bottom" title="${journal.name}">
                    ${journal.name}
            </div>
        </jsp:attribute>


    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">JOURNAL</div>
            <h1>${journal.name}</h1>
            <jsp:include page="journal-view-summary.jsp"/>
        </div>

        <z:section title="${PUBLICATION}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${journal.zdbID}"
                 showUnpublished="{false}"></div>
        </z:section>

    </jsp:body>

</z:dataPage>