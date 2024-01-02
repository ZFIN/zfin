<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.profile.Lab" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="GENOMIC_FEATURES" value="Genomic Features"/>
<c:set var="STATEMENT" value="Statement of Research Interest"/>
<c:set var="MEMBERS" value="Lab Members"/>
<c:set var="CITATIONS" value="Zebrafish Publications of lab members"/>

<c:set var="secs"
       value="${[SUMMARY, GENOMIC_FEATURES, STATEMENT, MEMBERS, CITATIONS]}"/>

<z:dataPage sections="${secs}" additionalBodyClass="lab-view nav-title-wrap">

    <jsp:attribute name="entityName">
                ${formBean.name}
    </jsp:attribute>

    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
            <nav class="navbar navbar-light admin text-center border-bottom">
                <a class="col-sm" href="/action/profile/lab/view/${formBean.zdbID}">Old View</a>
                <a class="col-sm" href="/action/profile/lab/edit/${formBean.zdbID}">Edit</a>
                <a class="col-sm" href="/action/infrastructure/deleteRecord/${formBean.zdbID}">Delete</a>
                <a class="col-sm" href='/action/profile/lab/all-labs'>All labs</a>
                <a class="col-sm" href='/action/profile/company/all-companies'>All companies</a>
                <a class="col-sm" href='/action/profile/person/all-people/A'>All people</a>
                <a class="col-md" href="/action/updates/${formBean.zdbID}">
                    Last Update:
                    <c:set var="latestUpdate" value="${zfn:getLastUpdate(formBean.zdbID)}"/>
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

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">Lab</div>
            <h1>${formBean.name}</h1>
            <jsp:include page="lab-view-summary.jsp"/>
        </div>

        <z:section title="${GENOMIC_FEATURES}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="FeatureLabTable" data-org-id="${formBean.zdbID}"></div>
        </z:section>

        <z:section title="${STATEMENT}">
            <div id='bio'><zfin2:splitLines input="${formBean.bio}"/></div>
        </z:section>

        <z:section title="${MEMBERS}">
            <zfin2:listMembersInTable members="${members}" greaterThan="2" columns="3"/>
        </z:section>

        <z:section title="${CITATIONS}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.zdbID}"></div>
        </z:section>


    </jsp:body>

</z:dataPage>
