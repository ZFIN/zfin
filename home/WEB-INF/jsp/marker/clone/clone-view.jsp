<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.CloneBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="SEQUENCES" value="Sequences"/>
<c:set var="GBROWSE" value="Genome Browser"/>
<c:set var="EXPRESSION" value="Gene Expression"/>
<c:set var="MARKERRELATIONSHIPS" value="Marker Relationships"/>


<z:dataPage
        sections="${[SUMMARY, GBROWSE, EXPRESSION, MARKERRELATIONSHIPS, SEQUENCES]}"
>
    <z:dataManagerDropdown>
        <a class="dropdown-item active" href="/action/marker/clone/prototype-view/${formBean.marker.zdbID}">View</a>
        <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
    </z:dataManagerDropdown>

    <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
    <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

    <div id="${zfn:makeDomIdentifier(SUMMARY)}">
    <jsp:include page="clone-view-summary.jsp" />
    </div>




    <z:section title="${GBROWSE}">
    <div class="summary">
        <div id="clone_gbrowse_thumbnail_box">
            <table class="summary solidblock">

                <tr>
                    <td style="text-align: center">
                        <div class="gbrowse-image"/>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <script>
        jQuery("#clone_gbrowse_thumbnail_box").gbrowseImage({
            width: 600,
            imageTarget: ".gbrowse-image",
            imageUrl: "${formBean.image.imageUrl}",
            linkUrl: "${formBean.image.linkUrl}"
        });
    </script>
    </z:section>

    <z:section title="${EXPRESSION}">
        <jsp:include page="clone-view-expression.jsp" />
    </z:section>

    <z:section title="${MARKERRELATIONSHIPS}">
        <div class="__react-root" id="GeneMarkerRelationshipsTable" data-gene-id="${formBean.marker.zdbID}"></div>
    </z:section>



    <z:section title="${SEQUENCES}">
        <div class="__react-root" id="GeneSequencesTable" data-gene-id="${formBean.marker.zdbID}"></div>
    </z:section>

</z:dataPage>
<script src="${zfn:getAssetPath("react.js")}"></script>
