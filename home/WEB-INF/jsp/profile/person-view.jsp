<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.profile.Person" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="BIOGRAPHY" value="BIOGRAPHY AND RESEARCH INTERESTS"/>
<c:set var="RESEARCH_INTEREST" value="Research Interests"/>
<c:set var="CITATIONS" value="Publications"/>
<c:set var="NON_ZFIN_CITATIONS" value="Non-Zebrafish Publications"/>

<c:set var="secs"
       value="${[SUMMARY, BIOGRAPHY, RESEARCH_INTEREST, CITATIONS, NON_ZFIN_CITATIONS]}"/>

<z:dataPage sections="${secs}">

    <jsp:attribute name="entityName">
                ${formBean.name}
    </jsp:attribute>

    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
            <nav class="navbar navbar-light admin text-center border-bottom">
                <a class="col-sm" href="/action/profile/lab/view/${formBean.zdbID}">Old View</a>
                <a class="col-sm" href="/action/publication/${formBean.zdbID}/edit">Edit</a>
                <a class="col-sm" href="/action/infrastructure/deleteRecord/${formBean.zdbID}">Delete</a>
                <a class="col-sm" href='/action/profile/lab/all-labs'>All labs</a>
                <a class="col-sm" href='/action/profile/company/all-companies'>All companies</a>
                <a class="col-sm" href='/action/profile/person/all-people/A'>All people</a>
            </nav>
        </authz:authorize>
    </jsp:attribute>

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">Person</div>
            <h1>${formBean.fullName}</h1>
<%--
            <jsp:include page="lab-view-summary.jsp"/>
--%>
        </div>

<%--
        <z:section title="${CITATIONS}" infoPopup="/action/marker/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.zdbID}"></div>
        </z:section>
--%>


    </jsp:body>

</z:dataPage>
