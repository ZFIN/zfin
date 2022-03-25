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
<c:set var="DISEASE" value="Disease"/>
<c:set var="ORTHOLOGY" value="Orthology"/>
<c:set var="MAPPING" value="Mapping"/>
<c:set var="DIRECTLY_ATTRIBUTED_DATA" value="Directly Attributed Data"/>
<c:set var="ERRATA" value="Errata and Notes"/>

<c:set var="secs"/>

<c:choose>
    <c:when test="${not empty publication.zebrashareEditors}">&nbsp;
        <c:set var="secs"
               value="${[SUMMARY, ABSTRACT, GENES, FIGURES, EXPRESSION, PHENOTYPE, MUTATION, STRS, DISEASE, FISH, ANTIBODIES, ORTHOLOGY, EFGs, MAPPING, DIRECTLY_ATTRIBUTED_DATA, ERRATA, ZEBRASHARE]}"/>
    </c:when>
    <c:otherwise>
        <c:set var="secs"
               value="${[SUMMARY, ABSTRACT, GENES, FIGURES, EXPRESSION, PHENOTYPE, MUTATION, STRS, DISEASE, FISH, ANTIBODIES, ORTHOLOGY, EFGs, MAPPING, DIRECTLY_ATTRIBUTED_DATA, ERRATA]}"/>
    </c:otherwise>
</c:choose>

<z:dataPage sections="${secs}">

    <jsp:attribute name="entityName">
        <div data-toggle="tooltip" data-placement="bottom" title="${publication.citation}">
                ${publication.shortAuthorList}
        </div>
        <div style="font-size: 12px">
                ${publication.zdbID}
            <c:if test="${!empty publication.accessionNumber}"><p/>PMID:${publication.accessionNumber}</c:if>
        </div>
    </jsp:attribute>

    <jsp:attribute name="pageBar">
        <nav class="navbar navbar-light admin text-center border-bottom">
            <a class="col-sm" href="/action/publication/view/${publication.zdbID}">Old View</a>
            <a class="col-sm" href="/action/curation/${publication.zdbID}">Curate</a>
            <a class="col-sm" href="/action/publication/${publication.zdbID}/link">Link</a>
            <a class="col-sm" href="/action/publication/${publication.zdbID}/edit">Edit</a>
            <c:choose>
            <c:when test="${allowDelete}">
                <a class="col-sm" href="/action/infrastructure/deleteRecord/${publication.zdbID}">Delete</a>
            </c:when>
                <c:otherwise>
                    <span class="col-sm">Delete</span>
                </c:otherwise>
            </c:choose>
            <a class="col-sm" href="/action/publication/${publication.zdbID}/track">Track</a>
        </nav>
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
            <z:section title="">
                <a href="/action/figure/all-figure-view/${publication.zdbID}" style="font-weight: bold">Show all
                    Expression and Phenotype Data </a>
            </z:section>
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

        <z:section title="${ORTHOLOGY}" >
            <div class="__react-root" id="PublicationOrthologyTable"
                 data-url="/action/api/publication/${publication.zdbID}/orthology"></div>
        </z:section>

        <z:section title="${EFGs}">
            <div class="__react-root" id="PublicationMarkerTable"
                 data-url="/action/api/publication/${publication.zdbID}/efgs"></div>
        </z:section>

        <z:section title="${DIRECTLY_ATTRIBUTED_DATA}">
            <div class="__react-root" id="PublicationAttributionTable"
                 data-url="/action/api/publication/${publication.zdbID}/direct-attribution"></div>
        </z:section>

        <z:section title="${ERRATA}">
            ${publication.errataAndNotes}
        </z:section>

        <z:section title="${ZEBRASHARE}">
            <zfin2:subsection title="" test="${not empty abstractText}" showNoData="true">
                <jsp:include page="publication-zebrashare.jsp"/>
            </zfin2:subsection>
        </z:section>


    </jsp:body>

</z:dataPage>
