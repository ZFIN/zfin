<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.AntibodyMarkerBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="NOTES" value="Notes"/>
<c:set var="LABELING" value="Anatomical Labeling"/>
<c:set var="CITATIONS" value="Citations"/>

<z:dataPage sections="${[SUMMARY, NOTES, LABELING, CITATIONS]}">
    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="${formBean.editURL}">Edit</a>
            <a class="dropdown-item" href="${formBean.deleteURL}">Delete</a>
            <a class="dropdown-item" href="${formBean.mergeURL}">Merge</a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" href="/action/marker/antibody/prototype-edit/${formBean.marker.zdbID}">Prototype Edit</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}" />
            <jsp:include page="antibody-view-summary.jsp"/>
        </div>

        <z:section title="${NOTES}">
            <jsp:include page="antibody-view-notes.jsp"/>
        </z:section>

       <z:section title="${LABELING}" infoPopup="/ZFIN/help_files/atb_expression_help.html">
            <z:section title="Antibody Labeling Summary">
                <div class="__react-root" id="GeneExpressionRibbon" data-gene-id="${formBean.marker.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="CitationTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>

    </jsp:body>
</z:dataPage>
