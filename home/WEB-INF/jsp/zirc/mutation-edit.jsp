<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="GENERAL" value="General"/>

<%-- Sections grow as the per-mutation editor implements them. Mutagenesis,
     Lethality, Publications, Genes, Lesions, Genotyping Assays and
     Phenotypes are next. --%>
<c:set var="sections" value="${[GENERAL]}"/>

<c:set var="alleleLabel" value="${not empty mutation.alleleDesignation
    ? mutation.alleleDesignation
    : 'Mutation #'.concat(mutation.sortOrder)}"/>

<z:dataPage sections="${sections}" title="Edit Mutation: ${alleleLabel}">

    <jsp:attribute name="entityName">${alleleLabel}</jsp:attribute>

    <jsp:attribute name="pageBar">
        <nav class="navbar navbar-light admin text-center border-bottom">
            <a class="col-sm" href="/action/zirc/dashboard">Dashboard</a>
            <a class="col-sm" href="/action/zirc/line-submission/${submission.zdbID}/edit">
                Submission
            </a>
            <span class="col-sm">Mutation</span>
        </nav>
    </jsp:attribute>

    <jsp:body>

        <div class="small text-uppercase text-muted">
            ${submission.zdbID} &middot; Edit Mutation
        </div>
        <h1>${alleleLabel}</h1>

        <%-- React mount: container at home/javascript/react/containers/MutationEdit.tsx. --%>
        <div class="__react-root" id="MutationEdit"
             data-mutation-id="${mutation.id}"
             data-submission-id="${submission.zdbID}"></div>

    </jsp:body>
</z:dataPage>
