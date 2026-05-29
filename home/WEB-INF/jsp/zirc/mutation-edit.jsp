<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="GENERAL"     value="General"/>
<c:set var="MUTAGENESIS" value="Mutagenesis"/>
<c:set var="GENES"       value="Genes"/>
<c:set var="LESIONS"     value="Lesions"/>
<c:set var="ASSAYS"      value="Genotyping Assays"/>
<c:set var="PHENOTYPES"  value="Phenotypes"/>
<c:set var="LETHALITY"   value="Lethality"/>
<c:set var="PUBS"        value="Publications"/>

<c:set var="sections" value="${[GENERAL, MUTAGENESIS, GENES, LESIONS, ASSAYS, PHENOTYPES, LETHALITY, PUBS]}"/>

<c:set var="alleleLabel" value="${not empty mutation.alleleDesignation
    ? mutation.alleleDesignation
    : 'Mutation #'.concat(mutation.sortOrder)}"/>

<z:dataPage sections="${sections}" title="Edit Mutation: ${alleleLabel}">

    <jsp:attribute name="entityName">${alleleLabel}</jsp:attribute>

    <jsp:attribute name="pageBar">
        <nav class="navbar navbar-light admin text-center border-bottom">
            <a class="col-sm" href="/action/zirc/dashboard">Dashboard</a>
            <a class="col-sm" href="/action/zirc/line-submission/${submission.zdbID}/edit">
                &laquo; Submission
            </a>
            <span class="col-sm">Mutation</span>
        </nav>
    </jsp:attribute>

    <jsp:body>

        <div class="small text-uppercase text-muted">
            Submission &rsaquo;
            <a href="/action/zirc/line-submission/${submission.zdbID}/edit">
                <c:out value="${not empty submission.name ? submission.name : submission.zdbID}"/>
            </a>
            &middot; Edit Mutation
        </div>
        <h1>${alleleLabel}</h1>

        <%-- React mount: container at home/javascript/react/containers/MutationEdit.tsx. --%>
        <div class="__react-root" id="MutationEdit"
             data-mutation-id="${mutation.id}"
             data-submission-id="${submission.zdbID}"></div>

        <div class="mt-4">
            <a class="btn btn-outline-secondary"
               href="/action/zirc/line-submission/${submission.zdbID}/edit">
                &laquo; Back to Submission
            </a>
        </div>

    </jsp:body>
</z:dataPage>
