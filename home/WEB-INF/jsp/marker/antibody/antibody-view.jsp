<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.AntibodyMarkerBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="NOTES" value="Notes"/>
<c:set var="LABELING" value="Anatomical Labeling"/>

<z:dataPage
        sections="${[SUMMARY, NOTES, LABELING]}">
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.editURL}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.deleteURL}">Delete</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.mergeURL}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
    </z:dataManagerDropdown>

    <div id="${zfn:makeDomIdentifier(SUMMARY)}">
        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <z:attributeList>
            <z:attributeListItem label="Antibody ID">
                ${formBean.marker.zdbID}
            </z:attributeListItem>

            <z:attributeListItem label="Antibody Name">
                ${formBean.marker.name}
            </z:attributeListItem>

            <z:attributeListItem label="Synonyms">
                <ul class="comma-separated">
                    <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                        <li>${markerAlias.linkWithAttribution}</li>
                    </c:forEach>
                </ul>
            </z:attributeListItem>

            <z:attributeListItem label="Host Organism">
                <div>${formBean.marker.hostSpecies}</div>
            </z:attributeListItem>

            <z:attributeListItem label="Immunogen Organism">
                <div>${formBean.marker.immunogenSpecies}</div>
            </z:attributeListItem>

            <z:attributeListItem label="Isotype">
                <div>${formBean.marker.heavyChainIsotype}, ${formBean.marker.lightChainIsotype}</div>
            </z:attributeListItem>

            <z:attributeListItem label="Type">
                <div>${formBean.marker.clonalType}</div>
            </z:attributeListItem>

            <z:attributeListItem label="Assays">
                <ul class="comma-separated">
                    <c:forEach var="gene" items="${formBean.marker.distinctAssayNames}" varStatus="loop">
                        <li>${gene}</li>
                    </c:forEach>
                </ul>
            </z:attributeListItem>

            <z:attributeListItem label="Antigen Genes">
                <ul class="comma-separated">
                    <c:forEach var="gene" items="${formBean.antigenGenes}">
                        <li><zfin:link entity="${gene}"/>${gene.attributionLink}</li>
                    </c:forEach>
                </ul>
            </z:attributeListItem>

            <z:attributeListItem label="Antibody Registry ID">
                <zfin2:externalLink
                        href="http://antibodyregistry.org/search.php?q=${formBean.abRegistryID}">${formBean.abRegistryID}</zfin2:externalLink>
            </z:attributeListItem>

            <z:attributeListItem label="Source">
                <zfin2:orderThis markerSuppliers="${formBean.suppliers}" accessionNumber="${formBean.marker.zdbID}"/>
            </z:attributeListItem>

            <z:attributeListItem label="Wiki">
                <script type="text/javascript">
                    jQuery(document).ready(function () {
                        jQuery('#wikiLink').load('/action/wiki/wikiLink/${formBean.marker.zdbID}');
                    });
                </script>
                <span id="wikiLink"> </span>
            </z:attributeListItem>

            <z:attributeListItem label="Citations">
                <a href="/action/antibody/antibody-publication-list?antibodyID=${formBean.marker.zdbID}&orderBy=author">(${formBean.numPubs})</a>
            </z:attributeListItem>

        </z:attributeList>
    </div>

    <z:section title="${NOTES}">
        <jsp:include page="antibody-view-notes.jsp"/>
    </z:section>

    <z:section title="${LABELING}">
        <jsp:include page="antibody-view-labeling.jsp"/>
    </z:section>


</z:dataPage>

<script src="${zfn:getAssetPath("react.js")}"></script>
