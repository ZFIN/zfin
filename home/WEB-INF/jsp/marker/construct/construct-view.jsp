<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="CONSTRUCTCOMPONENTS" value="Construct Components"/>
<c:set var="GENOMICFEATURES" value="Genomic Features"/>
<c:set var="TRANSGENICS" value="Transgenics"/>
<c:set var="SEQUENCEINFORMATION" value="Sequence Information"/>

<z:dataPage sections="${[SUMMARY, CONSTRUCTCOMPONENTS, GENOMICFEATURES, TRANSGENICS, SEQUENCEINFORMATION]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item active" href="/action/marker/construct/prototype-view/${formBean.marker.zdbID}">View</a>
            <div class="dropdown-divider"></div>

            <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
        </z:dataManagerDropdown>

        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <jsp:include page="construct-view-summary.jsp"/>
        </div>

        <z:section title="${GENOMICFEATURES}">
            <jsp:include page="construct-view-genomic-features.jsp"/>
        </z:section>

        <z:section title="${TRANSGENICS}">
            <jsp:include page="construct-view-transgenics.jsp"/>
        </z:section>

        <z:section title="${SEQUENCEINFORMATION}">
        <jsp:include page="construct-view-sequence-information.jsp"/>
        </z:section>
    </jsp:body>
</z:dataPage>

