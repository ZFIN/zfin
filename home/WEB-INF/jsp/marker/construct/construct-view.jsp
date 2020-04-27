<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="PLASMIDMAP" value="Plasmid Map"/>
<c:set var="GENOMICFEATURES" value="Genomic Features"/>
<c:set var="TRANSGENICS" value="Transgenics"/>
<c:set var="SEQUENCEINFORMATION" value="Sequence Information"/>
<c:set var="OTHERPAGES" value="Other Construct Pages"/>

<z:dataPage sections="${[SUMMARY, PLASMIDMAP, GENOMICFEATURES, TRANSGENICS, SEQUENCEINFORMATION, OTHERPAGES]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item active"
               href="/action/marker/construct/prototype-view/${formBean.marker.zdbID}">View</a>
            <div class="dropdown-divider"></div>

            <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
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
            <div class="__react-root" id="OtherMarkerSequencesTable" data-marker-id="${formBean.marker.zdbID}"></div>

        </z:section>

        <z:section title="${OTHERPAGES}">
            <jsp:include page="construct-view-other-pages.jsp"/>
        </z:section>
</jsp:body>
</z:dataPage>

