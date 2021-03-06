<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ABSTRACT" value="Abstract"/>
<c:set var="ZEBRASHARE" value="Zebrashare Submission Details"/>
<c:set var="FIGURES" value="Figures"/>
<c:set var="GENES" value="Genes / Markers"/>
<c:set var="STRS" value="Sequence Targeting Reagents"/>
<c:set var="ANTIBODIES" value="Antibodies"/>
<c:set var="EFGs" value="Engineered Foreign Genes"/>
<c:set var="EXPRESSION" value="Expression"/>
<c:set var="PHENOTYPE" value="Phenotype"/>
<c:set var="MUTATION" value="Mutation and Transgenics"/>
<c:set var="FISH" value="Fish"/>
<c:set var="DISEASE" value="Human Disease / Model Data"/>
<c:set var="ORTHOLOGY" value="Orthology"/>
<c:set var="DIRECTLY_ATTRIBUTED_DATA" value="Directly Attributed Data"/>
<c:set var="ERRATA" value="Errata and Notes"/>

<c:set var="secs"/>

<c:choose>
    <c:when test="${not empty publication.zebrashareEditors}">&nbsp;
        <authz:authorize access="isAuthenticated()">
            <c:set var="secs"
                   value="${[SUMMARY, ABSTRACT, GENES, FIGURES, EXPRESSION, PHENOTYPE, MUTATION, DISEASE, STRS, FISH, ANTIBODIES, ORTHOLOGY, EFGs, DIRECTLY_ATTRIBUTED_DATA, ERRATA, ZEBRASHARE]}"/>
        </authz:authorize>
        <authz:authorize access="!isAuthenticated()">
            <c:set var="secs"
                   value="${[SUMMARY, ABSTRACT, GENES, FIGURES, EXPRESSION, PHENOTYPE, MUTATION, DISEASE, STRS, FISH, ANTIBODIES, ORTHOLOGY, EFGs, ERRATA, ZEBRASHARE]}"/>
        </authz:authorize>
    </c:when>
    <c:otherwise>
        <authz:authorize access="isAuthenticated()">
            <c:set var="secs"
                   value="${[SUMMARY, ABSTRACT, GENES, FIGURES, EXPRESSION, PHENOTYPE, MUTATION, DISEASE, STRS, FISH, ANTIBODIES, ORTHOLOGY, EFGs, DIRECTLY_ATTRIBUTED_DATA, ERRATA]}"/>
        </authz:authorize>
        <authz:authorize access="!isAuthenticated()">
            <c:set var="secs"
                   value="${[SUMMARY, ABSTRACT, GENES, FIGURES, EXPRESSION, PHENOTYPE, MUTATION, DISEASE, STRS, FISH, ANTIBODIES, ORTHOLOGY, EFGs, ERRATA]}"/>
        </authz:authorize>
    </c:otherwise>
</c:choose>

<z:dataPage sections="${secs}">

    <jsp:attribute name="entityName">
        <div data-toggle="tooltip" data-placement="bottom" title="${publication.citation}">
                ${publication.shortAuthorList}
        </div>
    </jsp:attribute>
    <jsp:attribute name="entityNameAddendum">
        <div style="font-size: 12px">
                ${publication.zdbID}
            <c:if test="${!empty publication.accessionNumber}"><br/>PMID:${publication.accessionNumber}</c:if>
        </div>
    </jsp:attribute>

    <jsp:attribute name="pageBar">
        <authz:authorize access="hasRole('root')">
            <nav class="navbar navbar-light admin text-center border-bottom">
                <a class="col-sm" href="/action/publication/${publication.zdbID}/edit">Edit</a>
                <a class="col-sm" href="/action/publication/${publication.zdbID}/track">Track</a>
                <a class="col-sm" href="/action/publication/${publication.zdbID}/link">Link</a>
                <a class="col-sm" href="/action/curation/${publication.zdbID}">Curate</a>
                <c:if test="${hasCorrespondence}">
                <a class="col-sm" href="/action/publication/${publication.zdbID}/track#correspondence">
                    <i class="far fa-envelope"></i></a>
                </c:if>
                <c:choose>
                <c:when test="${allowDelete}">
                    <a class="col-sm" href="/action/infrastructure/deleteRecord/${publication.zdbID}">Delete</a>
                </c:when>
                    <c:otherwise>
                        <span class="col-sm">Delete</span>
                    </c:otherwise>
                </c:choose>
            </nav>
        </authz:authorize>
    </jsp:attribute>

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">PUBLICATION</div>
            <h1>${publication.title}</h1>
            <jsp:include page="publication-view-summary.jsp"/>
        </div>

        <z:section title="${ABSTRACT}">
            <zfin2:subsection title="" test="${not empty abstractText}" showNoData="true">
                ${abstractText}
            </zfin2:subsection>
        </z:section>

        <z:section title="${GENES}">
            <div class="__react-root" id="PublicationMarkerTable"
                 data-url="/action/api/publication/${publication.zdbID}/marker"></div>
        </z:section>

        <z:section title="${FIGURES}" infoPopup="/ZFIN/help_files/expression_help.html">
            <jsp:include page="publication-image-gallery.jsp"/>
        </z:section>

        <z:section title="${EXPRESSION}">
            <div class="__react-root" id="FigureExpressionTable"
                 data-url="/action/api/publication/${publication.zdbID}/expression"></div>
        </z:section>

        <z:section title="${PHENOTYPE}">
            <div class="__react-root" id="FigurePhenotypeTable"
                 data-url="/action/api/publication/${publication.zdbID}/phenotype"></div>
        </z:section>

        <z:section title="${MUTATION}">
            <div class="__react-root" id="PublicationMutationTable"
                 data-url="/action/api/publication/${publication.zdbID}/features"></div>
        </z:section>

        <z:section title="${DISEASE}">
            <div class="__react-root" id="PublicationDiseaseTable"
                 data-url="/action/api/publication/${publication.zdbID}/diseases"></div>
        </z:section>

        <z:section title="${STRS}">
            <div class="__react-root" id="StrTable"
                 data-url="/action/api/publication/${publication.zdbID}/strs"></div>
        </z:section>

        <z:section title="${FISH}">
            <div class="__react-root" id="PublicationFishTable"
                 data-url="/action/api/publication/${publication.zdbID}/fish"></div>
        </z:section>

        <z:section title="${ANTIBODIES}" infoPopup="/action/marker/note/antibodies">
            <div class="__react-root" id="AntibodyTable"
                 data-url="/action/api/publication/${publication.zdbID}/antibodies"></div>
        </z:section>

        <z:section title="${ORTHOLOGY}">
            <div class="__react-root" id="PublicationOrthologyTable"
                 data-url="/action/api/publication/${publication.zdbID}/orthology"></div>
        </z:section>

        <z:section title="${EFGs}">
            <div class="__react-root" id="PublicationMarkerTable"
                 data-url="/action/api/publication/${publication.zdbID}/efgs"></div>
        </z:section>

        <authz:authorize access="hasRole('root')">
            <z:section title="${DIRECTLY_ATTRIBUTED_DATA}">
                <div class="__react-root" id="PublicationAttributionTable"
                     data-url="/action/api/publication/${publication.zdbID}/direct-attribution"></div>
            </z:section>
        </authz:authorize>

        <z:section title="${ERRATA}">
            ${publication.errataAndNotes}
        </z:section>

        <c:if test="${not empty publication.zebrashareEditors}">
            <z:section title="${ZEBRASHARE}">
                <zfin2:subsection title="" showNoData="true">
                    <jsp:include page="publication-zebrashare.jsp"/>
                </zfin2:subsection>
            </z:section>
        </c:if>

    </jsp:body>

</z:dataPage>
