<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="person" class="org.zfin.profile.Person" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="PRODUCTS" value="Products and Services"/>
<c:set var="CITATIONS" value="Zebrafish Publications of Company Representatives"/>
<c:set var="REPRESENTATIVE" value="Company Representative"/>

<c:set var="secs"
       value="${[SUMMARY, PRODUCTS, REPRESENTATIVE, CITATIONS]}"/>

<z:dataPage sections="${secs}">

    <jsp:attribute name="entityName">
                ${company.name}
    </jsp:attribute>

    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
            <nav class="navbar navbar-light admin text-center border-bottom">
                <a class="col-sm" href="/action/profile/company/view/${company.zdbID}">Old View</a>
                <a class="col-sm" href="/action/profile/company/edit/${company.zdbID}">Edit</a>
                <a href="javascript:" class="root" onclick="location.replace('/action/infrastructure/deleteRecord/${company.zdbID}');">Delete</a>
                <a class="col-sm" href='/action/profile/lab/all-labs'>All labs</a>
                <a class="col-sm" href='/action/profile/company/all-companies'>All companies</a>
                <a class="col-sm" href='/action/profile/person/all-people/A'>All people</a>
                <a class="col-sm" href="/action/updates/${company.zdbID}">
                    Last Update:
                    <c:set var="latestUpdate" value="${zfn:getLastUpdate(company.zdbID)}"/>
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
            <div class="small text-uppercase text-muted">Company</div>
            <h1>${company.name}
                <c:if test="${person.deceased}"> (Deceased) </c:if>
            </h1>
            <jsp:include page="company-view-summary.jsp"/>
        </div>

        <z:section title="${PRODUCTS}">
            <div id='bio'><zfin2:splitLines input="${company.bio}"/></div>
        </z:section>

        <z:section title="${REPRESENTATIVE}">
            <zfin2:listMembersInTable members="${members}" columns="3"/>
        </z:section>

        <z:section title="${CITATIONS}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${company.zdbID}"></div>
        </z:section>

    </jsp:body>

</z:dataPage>
