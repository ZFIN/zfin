<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>
<%@ page import="org.zfin.framework.presentation.NavigationMenuOptions" %>
<%@ page import="org.zfin.framework.featureflag.FeatureFlagEnum" %>

<%--Create shorter variable names for the enum values of navigation menu options / section titles from enum--%>
<c:set var="SUMMARY" value="${NavigationMenuOptions.SUMMARY.value}"/>
<c:set var="ABSTRACT" value="${NavigationMenuOptions.ABSTRACT.value}"/>
<c:set var="ERRATA" value="${NavigationMenuOptions.ERRATA.value}"/>
<c:set var="GENES" value="${NavigationMenuOptions.GENES.value}"/>
<c:set var="FIGURES" value="${NavigationMenuOptions.FIGURES.value}"/>
<c:set var="PROBES" value="${NavigationMenuOptions.PROBES.value}"/>
<c:set var="EXPRESSION" value="${NavigationMenuOptions.EXPRESSION.value}"/>
<c:set var="PHENOTYPE" value="${NavigationMenuOptions.PHENOTYPE.value}"/>
<c:set var="MUTATION" value="${NavigationMenuOptions.MUTATION.value}"/>
<c:set var="DISEASE" value="${NavigationMenuOptions.DISEASE.value}"/>
<c:set var="STRS" value="${NavigationMenuOptions.STRS.value}"/>
<c:set var="FISH" value="${NavigationMenuOptions.FISH.value}"/>
<c:set var="ANTIBODIES" value="${NavigationMenuOptions.ANTIBODIES.value}"/>
<c:set var="ORTHOLOGY" value="${NavigationMenuOptions.ORTHOLOGY.value}"/>
<c:set var="EFGs" value="${NavigationMenuOptions.EFGs.value}"/>
<c:set var="MAPPING" value="${NavigationMenuOptions.MAPPING.value}"/>
<c:set var="DIRECTLY_ATTRIBUTED_DATA" value="${NavigationMenuOptions.DIRECTLY_ATTRIBUTED_DATA.value}"/>
<c:set var="ZEBRASHARE" value="${NavigationMenuOptions.ZEBRASHARE.value}"/>

<c:set var="BODYCLASSES" value="publication-view nav-title-wrap-break-word"/>
<c:if test="${zfn:isFlagEnabled(FeatureFlagEnum.USE_NAVIGATION_COUNTER)}">
    <c:set var="BODYCLASSES" value="${BODYCLASSES} show-navigation-counters"/>
</c:if>

<z:dataPage sections="${[]}" navigationMenu="${navigationMenu}" additionalBodyClass="${BODYCLASSES}">

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

        <z:section title="${ERRATA}"
                   navigationMenu="${navigationMenu}">
            ${publication.errataAndNotes}
        </z:section>

        <z:section title="${GENES}">
            <div class="__react-root __use-navigation-counter" id="PublicationMarkerTable__0"
                 data-url="/action/api/publication/${publication.zdbID}/marker"
                 data-title="${GENES}"
            ></div>
        </z:section>

        <z:section title="${PROBES}">
            <div class="__react-root __use-navigation-counter" id="PublicationProbeTable__0"
                 data-url="/action/api/publication/${publication.zdbID}/probes"
                 data-publication-id="${publication.zdbID}"
                 data-title="${PROBES}"
            ></div>
        </z:section>

        <z:section title="${FIGURES}" infoPopup="/ZFIN/help_files/expression_help.html">
            <div class="__react-root __use-navigation-counter" id="PublicationFigureDisplay"
                 data-title="${FIGURES}"
                 data-images-json="${imagesJson}"
                 data-publication-id="${publication.zdbID}"
            ></div>
            <c:if test="${!isLargeDataPublication}">
                <div><a href="/action/publication/${publication.zdbID}/all-figures">Show all Figures</a></div>
            </c:if>
        </z:section>

        <z:section title="${EXPRESSION}">
            <div class="__react-root __use-navigation-counter" id="FigureExpressionTable"
                 data-url="/action/api/publication/${publication.zdbID}/expression"
                 data-title="${EXPRESSION}"
            ></div>
        </z:section>

        <z:section title="${PHENOTYPE}">
            <div class="__react-root __use-navigation-counter" id="FigurePhenotypeTable"
                 data-url="/action/api/publication/${publication.zdbID}/phenotype"
                 data-title="${PHENOTYPE}"
            ></div>
        </z:section>

        <z:section title="${MUTATION}">
            <div class="__react-root __use-navigation-counter" id="PublicationMutationTable"
                 data-url="/action/api/publication/${publication.zdbID}/features"
                 data-title="${MUTATION}"
            ></div>
        </z:section>

        <z:section title="${DISEASE}">
            <div class="__react-root __use-navigation-counter" id="PublicationDiseaseTable"
                 data-url="/action/api/publication/${publication.zdbID}/diseases"
                 data-title="${DISEASE}"
            ></div>
        </z:section>

        <z:section title="${STRS}">
            <div class="__react-root __use-navigation-counter" id="StrTable"
                 data-url="/action/api/publication/${publication.zdbID}/strs"
                 data-title="${STRS}"
            ></div>
        </z:section>

        <z:section title="${FISH}">
            <div class="__react-root __use-navigation-counter" id="PublicationFishTable"
                 data-url="/action/api/publication/${publication.zdbID}/fish"
                 data-title="${FISH}"
            ></div>
        </z:section>

        <z:section title="${ANTIBODIES}" infoPopup="/action/marker/note/antibodies">
            <div class="__react-root __use-navigation-counter" id="AntibodyTable"
                 data-url="/action/api/publication/${publication.zdbID}/antibodies"
                 data-title="${ANTIBODIES}"
            ></div>
        </z:section>

        <z:section title="${ORTHOLOGY}">
            <div class="__react-root __use-navigation-counter" id="PublicationOrthologyTable"
                 data-url="/action/api/publication/${publication.zdbID}/orthology"
                 data-title="${ORTHOLOGY}"
            ></div>
        </z:section>

        <z:section title="${EFGs}">
            <div class="__react-root __use-navigation-counter" id="PublicationMarkerTable__1"
                 data-url="/action/api/publication/${publication.zdbID}/efgs"
                 data-title="${EFGs}"
            ></div>
        </z:section>

        <z:section title="${MAPPING}">
            <div class="__react-root __use-navigation-counter" id="PublicationMappingTable"
                 data-url="/action/api/publication/${publication.zdbID}/mapping"
                 data-title="${MAPPING}"
            ></div>
        </z:section>

        <z:section title="${DIRECTLY_ATTRIBUTED_DATA}" navigationMenu="${navigationMenu}">
            <div class="__react-root __use-navigation-counter" id="PublicationAttributionTable"
                 data-url="/action/api/publication/${publication.zdbID}/direct-attribution"
                 data-title="${DIRECTLY_ATTRIBUTED_DATA}"
            ></div>
        </z:section>

        <z:section title="${ZEBRASHARE}" navigationMenu="${navigationMenu}">
            <zfin2:subsection title="" showNoData="true">
                <jsp:include page="publication-zebrashare.jsp"/>
            </zfin2:subsection>
        </z:section>

    </jsp:body>

</z:dataPage>
