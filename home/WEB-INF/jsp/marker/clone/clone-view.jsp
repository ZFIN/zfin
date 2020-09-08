<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.CloneBean" scope="request"/>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="GBROWSE" value="Genome Browser"/>
<c:set var="EXPRESSION" value="Gene Expression"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>
<c:set var="CITATIONS" value="Citations"/>

<c:set var="typeName">${formBean.marker.markerType.name}</c:set>
<c:if test="${typeName ne 'EST' || typeName ne 'CDNA'}">
    <c:set var="sections" value="${[SUMMARY, GBROWSE, MARKERRELATIONSHIPS, SEQUENCES, CITATIONS]}"/>
</c:if>
<c:if test="${typeName eq 'EST' || typeName eq 'CDNA'}">
    <c:set var="sections" value="${[SUMMARY, EXPRESSION, MARKERRELATIONSHIPS, SEQUENCES, CITATIONS]}"/>
</c:if>

<z:dataPage sections="${sections}">

    <jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/marker/marker-edit?zdbID=${formBean.marker.zdbID}">Edit</a>
            <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <zfin2:markerDataPageHeader marker="${formBean.marker}" />
            <jsp:include page="clone-view-summary.jsp"/>
        </div>
        
        <c:if test="${typeName ne 'EST'}">
           <c:if test="${typeName ne 'CDNA'}">
            <z:section title="${GBROWSE}">
               <div class="__react-root"
                 id="GbrowseImage"
                 data-image-url="${formBean.image.imageUrl}"
                 data-link-url="${formBean.image.linkUrl}">
              </div>
            </z:section>
           </c:if>
        </c:if>

        <c:if test="${typeName eq 'EST' || typeName eq 'CDNA'}">
            <z:section title="${EXPRESSION}">
               <jsp:include page="clone-view-expression.jsp"/>
            </z:section>
        </c:if>

        <z:section title="${MARKERRELATIONSHIPS}">
            <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
        </z:section>

        <z:section title="${SEQUENCES}">
            <div
                class="__react-root"
                id="MarkerSequencesTable"
                data-marker-id="${formBean.marker.zdbID}"
            >
            </div>
        </z:section>

        <z:section title="${CITATIONS}">
            <div class="__react-root" id="MarkerCitationsTable" data-marker-id="${formBean.marker.zdbID}"></div>
        </z:section>
    </jsp:body>
</z:dataPage>
