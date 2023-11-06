<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="PLASMIDMAP" value="Plasmid Map"/>
<c:set var="GENOMICFEATURES" value="Genomic Features"/>
<c:set var="TRANSGENICS" value="Transgenics"/>
<c:set var="SEQUENCEINFORMATION" value="Sequence Information"/>
<c:set var="OTHERPAGES" value="Other Construct Pages"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, PLASMIDMAP, GENOMICFEATURES, TRANSGENICS, SEQUENCEINFORMATION, OTHERPAGES, CITATIONS]}"
            additionalBodyClass="construct-view nav-title-wrap">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/infrastructure/deleteRecord/${formBean.marker.zdbID}"><i class="fas fa-trash"></i> Delete</a>
            <a class="dropdown-item" href="/action/updates/${formBean.marker.zdbID}"><i class="fas fa-clock"></i> History</a>
            <a class="dropdown-item" href="/action/nomenclature/history/${formBean.marker.zdbID}"><i class="fas fa-tags"></i> Nomenclature History</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}" />
            <jsp:include page="construct-view-summary.jsp"/>
        </div>

        <z:section title="${PLASMIDMAP}">
            <jsp:include page="construct-view-plasmid-map.jsp"/>
        </z:section>

        <z:section title="${GENOMICFEATURES}">
            <z:section title="That Utilize ${formBean.marker.abbreviation}</i> Construct">
                <jsp:include page="construct-view-genomic-features.jsp"/>
            </z:section>
        </z:section>

        <z:section title="${TRANSGENICS}">
            <z:section title="That Utilize ${formBean.marker.abbreviation}</i> Construct">
                <jsp:include page="construct-view-transgenics.jsp"/>
            </z:section>
        </z:section>

        <z:section title="${SEQUENCEINFORMATION}">
            <div
                class="__react-root"
                id="MarkerSequencesTable"
                data-marker-id="${formBean.marker.zdbID}"
            >
            </div>
        </z:section>

        <z:section title="${OTHERPAGES}">
            <jsp:include page="construct-view-other-pages.jsp"/>
        </z:section>
        
        <z:section title="${CITATIONS}">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>

