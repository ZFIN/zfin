<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="GENERAL"   value="General"/>
<c:set var="LETHALITY" value="Lethality"/>

<c:set var="sections" value="${[GENERAL, LETHALITY]}"/>

<c:choose>
    <c:when test="${not empty mutation.alleleDesignation}">
        <c:set var="entityLabel" value="${mutation.alleleDesignation}"/>
    </c:when>
    <c:otherwise>
        <c:set var="entityLabel" value="Mutation #${mutation.sortOrder}"/>
    </c:otherwise>
</c:choose>

<z:dataPage sections="${sections}" title="Edit Mutation: ${entityLabel}">

    <jsp:attribute name="entityName">${entityLabel}</jsp:attribute>

    <jsp:attribute name="pageBar">
        <nav class="navbar navbar-light admin text-center border-bottom">
            <a class="col-sm" href="/action/zirc/dashboard">Dashboard</a>
            <a class="col-sm" href="/action/zirc/line-submission/${submission.zdbID}/edit">&laquo; Submission</a>
            <span class="col-sm">Mutation</span>
        </nav>
    </jsp:attribute>

    <jsp:body>

        <div class="small text-uppercase text-muted">
            Edit Mutation
            <c:if test="${not empty submission.name}">
                &mdash; <a href="/action/zirc/line-submission/${submission.zdbID}/edit"><c:out value="${submission.name}"/></a>
            </c:if>
        </div>
        <h1>${entityLabel}</h1>

        <div class="__react-root" id="MutationEdit"
             data-mutation-id="${mutation.id}"></div>

    </jsp:body>
</z:dataPage>
