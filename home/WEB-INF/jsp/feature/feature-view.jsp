<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="GENOTYPE" value="Genotypes"/>
<c:set var="VARIANTS" value="Variants"/>
<c:set var="MUTATION_DETAILS" value="Mutation Details"/>
<c:set var="FISH" value="Fish"/>
<c:set var="SUPPLEMENTAL" value="Supplemental Information"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage
        sections="${[SUMMARY, VARIANTS, SUPPLEMENTAL, CITATIONS]}"
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

        <z:section title="${VARIANTS}" >
            <z:section title="">
            <jsp:include page="feature-view-variants.jsp"/>
        </z:section>
            <z:section title="Effect on DNA/cDNA, transcript, protein (from publications)">
                <jsp:include page="feature-view-mut-details.jsp"/>
            </z:section>
        </z:section>


        <z:section title="${SUPPLEMENTAL}" >
            <z:section title="">
                <jsp:include page="feature-view-zirc.jsp"/>
            </z:section>
            <c:if  test="${!empty formBean.ftrCommContr}">
            <z:section title="Zebrashare Submission <a href='/${formBean.ZShareOrigPub.zdbID}'>(1)"></a>
                <jsp:include page="feature-view-supplemental.jsp"/>
            </z:section>
            </c:if>
        </z:section>

        <z:section title="${CITATIONS}" >
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.feature.zdbID}" ></div>
        </z:section>

    </jsp:body>

</z:dataPage>
