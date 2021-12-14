<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ABSTRACT" value="Abstract"/>
<c:set var="FIGURES" value="Figures"/>
<c:set var="GENES" value="Genes / Markers"/>
<c:set var="ANTIBODIES" value="Antibodies"/>
<c:set var="EXPRESSION" value="Expression Data"/>
<c:set var="MUTATION" value="Mutation and Transgenics"/>
<c:set var="FISH" value="Fish"/>
<c:set var="DIRECTLY_ATTRIBUTED_DATA" value="Driectly Attributed Data"/>

<z:dataPage
        sections="${[SUMMARY, ABSTRACT, FIGURES, GENES, ANTIBODIES, EXPRESSION, MUTATION, FISH, DIRECTLY_ATTRIBUTED_DATA]}">

    <jsp:attribute name="entityName">
        ${publication.title}
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/publication/view/${publication.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">PUBLICATION</div>
            <h1>${publication.title}</h1>
            <div style="text-align: center; font-weight: bold">
                    ${publication.authors}
            </div>
            <jsp:include page="publication-view-summary.jsp"/>
        </div>

        <z:section title="${ABSTRACT}">
            <zfin2:subsection title="" test="${not empty abstractText}" showNoData="true">
                ${abstractText}
            </zfin2:subsection>
        </z:section>

        <z:section title="${FIGURES}" infoPopup="/ZFIN/help_files/expression_help.html">
            <%--
                        <z:section title="">
                            <jsp:include page="fish-view-human-disease.jsp"/>
                        </z:section>
            --%>
        </z:section>

        <z:section title="${GENES}">
            <div class="__react-root" id="PublicationMarkerTable" data-url="/action/api/publication/${publication.zdbID}/marker"></div>
        </z:section>

        <z:section title="${ANTIBODIES}" infoPopup="/action/marker/note/antibodies">
            <div class="__react-root" id="AntibodyTable" data-url="/action/api/publication/${publication.zdbID}/antibodies"></div>
        </z:section>

        <z:section title="${MUTATION}">
            <div class="__react-root" id="PublicationMutationTable" data-url="/action/api/publication/${publication.zdbID}/features"></div>
        </z:section>


    </jsp:body>

</z:dataPage>
