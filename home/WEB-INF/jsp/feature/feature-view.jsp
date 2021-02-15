<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="GENOTYPE" value="Genotypes"/>
<c:set var="VARIANTS" value="Variants"/>
<c:set var="MUTATION_DETAILS" value="Mutation Details"/>
<c:set var="FISH" value="Fish"/>
<c:set var="SUPPLEMENTAL" value="Supplemental Information"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="GBROWSE" value="Genome Browser"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage
        sections="${[SUMMARY, GBROWSE, VARIANTS, SEQUENCES, FISH, SUPPLEMENTAL, CITATIONS]}"
>
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.feature}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/${formBean.feature.zdbID}">Classic View</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">Genomic Feature</div>
            <h1><zfin:abbrev entity="${formBean.feature}"/></h1>
            <jsp:include page="feature-view-summary.jsp"/>
        </div>

        <z:section title="${GBROWSE}" infoPopup="/action/feature/note/genomebrowser">>
            <div class="__react-root"
                 id="GbrowseImage"
                 data-image-url="${formBean.GBrowseImage.imageUrl}"
                 data-link-url="${formBean.GBrowseImage.linkUrl}"
                 data-build="${formBean.GBrowseImage.build}"
            >
            </div>
        </z:section>

        <z:section title="${VARIANTS}" >
            <jsp:include page="feature-view-variants.jsp"/>

            <z:section title="Effect on DNA/cDNA, transcript, protein (from publications)">
                <jsp:include page="feature-view-mut-details.jsp"/>
            </z:section>
        </z:section>

        <z:section title="${SEQUENCES}" infoPopup="/action/feature/flank-seq">
            <jsp:include page="feature-view-sequence.jsp"/>
        </z:section>

        <z:section title="${FISH}">
            <div class="__react-root" id="FeatureFishTable" data-feature-id="${formBean.feature.zdbID}"></div>
        </z:section>

        <z:section title="${SUPPLEMENTAL}" >
            <jsp:include page="feature-view-zirc.jsp"/>

            <c:if test="${!empty formBean.ftrCommContr}">
                <z:section>
                    <jsp:attribute name="title">
                        ZebraShare Submission <a href='/${formBean.ZShareOrigPub.zdbID}'>(1)</a>
                    </jsp:attribute>
                    <jsp:body>
                        <jsp:include page="feature-view-supplemental.jsp"/>
                    </jsp:body>
                </z:section>
            </c:if>
        </z:section>

        <z:section title="${CITATIONS}" infoPopup="/action/feature/note/citations">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.feature.zdbID}" ></div>
        </z:section>

    </jsp:body>

</z:dataPage>
