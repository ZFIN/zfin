<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

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
        <zfin2:externalLink href="http://antibodyregistry.org/search.php?q=${formBean.abRegistryID}">
            ${formBean.abRegistryID}
        </zfin2:externalLink>
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