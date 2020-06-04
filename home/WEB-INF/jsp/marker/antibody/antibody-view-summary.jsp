<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="Antibody ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Antibody Name">
        ${formBean.marker.name}
    </z:attributeListItem>
    
    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}" />

    <z:attributeListItem label="Host Organism">
        <div>${formBean.marker.hostSpecies}</div>
    </z:attributeListItem>

    <z:attributeListItem label="Immunogen Organism">
        <div>${formBean.marker.immunogenSpecies}</div>
    </z:attributeListItem>

    <z:attributeListItem label="Isotype">
        <div>${formBean.marker.heavyChainIsotype}
            <c:if  test="${formBean.marker.heavyChainIsotype != null && formBean.marker.lightChainIsotype != null}">,
            </c:if>${formBean.marker.lightChainIsotype}
        </div>
    </z:attributeListItem>

    <z:attributeListItem label="Type">
        <div>${formBean.marker.clonalType}</div>
    </z:attributeListItem>

    <z:attributeListItem label="Assays">
        <ul class="comma-separated">
            <c:forEach var="gene" items="${formBean.marker.distinctAssayNames}">
                <li>${gene}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Antigen Genes">
        <ul class="comma-separated">
            <c:forEach var="gene" items="${formBean.antigenGenes}">
                <li><zfin:link entity="${gene}"/> ${gene.attributionLink}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Antibody Registry ID">
        <z:ifHasData test="${formBean.abRegistryID != null}" noDataMessage="None">
            <c:set var="url">http://antibodyregistry.org/search.php?q=${formBean.abRegistryID}</c:set>
            <zfin2:externalLink href="${url}">${formBean.abRegistryID}</zfin2:externalLink>
        </z:ifHasData>
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
    
    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}" />
</z:attributeList>