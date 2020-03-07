<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="TARGETLOCATION" value="Target Location"/>
<c:set var="CONSTRUCTS" value="Constructs With Sequences"/>
<c:set var="EXPRESSION" value="Expression"/>
<c:set var="PHENOTYPE" value="Phenotype"/>

<z:dataPage
        sections="${[SUMMARY, TARGETLOCATION, CONSTRUCTS, EXPRESSION, PHENOTYPE]}">
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
        <z:attributeListItem label="ID">
            ${formBean.marker.zdbID}
        </z:attributeListItem>

        <z:attributeListItem label="Name">
            ${formBean.marker.name}
        </z:attributeListItem>

        <z:attributeListItem label="Synonyms">
            <ul class="comma-separated">
                <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                    <li>${markerAlias.linkWithAttribution}</li>
                </c:forEach>
            </ul>
        </z:attributeListItem>

        <c:if test="${empty transcript}">
            no sequence available
        </c:if>

        <c:if test="${(formBean.markerRelationshipPresentationList)>1}">
            <c:set var="targetsLabel" value="Targets"/>
            <c:else>
                <c:set var="targetsLabel" value="Target"/>
            </c:else>
        </c:if>

        <z:attributeListItem label="${targetsLabel}">
            <div>${formBean.marker.}</div>
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

    <script>
jQuery(function () {
    jQuery("#genotype").find(".summary").tableCollapse({label: "genotypes"});
});
</script>



<c:set var="editURL">/action/str/${formBean.marker.zdbID}/edit</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}</c:set>
<script>
if (opener != null)
    opener.fireCreateMarkerEvent();
</script>


<div id="gbrowse-images" class="summary">
<div class="summaryTitle">
    TARGET LOCATION${fn:length(formBean.gbrowseImages) == 1 ? "" : "S"}
    <a class="popup-link info-popup-link" href="/action/marker/note/sequence-targeting-reagent-gbrowse"></a>
</div>

<c:forEach items="${formBean.gbrowseImages}" var="image" end="1">
    <div class="gbrowse-image"
         data-gbrowse-image='{"imageUrl": "${image.imageUrl}", "linkUrl": "${image.linkUrl}", "build": "${image.build}"}'>
    </div>
</c:forEach>

<c:if test="${fn:length(formBean.gbrowseImages) > 2}">
    <div>
        <a href="/action/marker/view/${formBean.marker.zdbID}/str-targeted-genes">View
            all ${fn:length(formBean.gbrowseImages)} target locations</a>
    </div>
</c:if>

<span id="gbrowse-no-data" class="no-data-tag">No data available</span>
</div>

<script>
jQuery(".gbrowse-image").gbrowseImage({
    success: function () {
        jQuery("#gbrowse-no-data").hide();
    }
});
</script>
<zfin2:constructsWithSequences formBean="${formBean}"/>
<%--// GENOTYPE CREATED BY TALEN OR CRISPR --%>
<c:if test="${formBean.marker.markerType.name eq 'TALEN' || formBean.marker.markerType.name eq 'CRISPR'}">
<div id="genomicFeature" class="summary">
    <zfin2:subsection title="GENOMIC FEATURES CREATED WITH ${formBean.marker.name}"
                      test="${!empty formBean.genomicFeatures}" showNoData="true">
        <table id="features-table" class="summary rowstripes">
            <tr>
                <th width="25%">
                    Genomic Feature
                </th>
                <th width="25%">
                    Affected Genomic Regions
                </th>
                <th width="25%">
                    &nbsp;
                </th>
                <th width="25%">
                    &nbsp;
                </th>
            </tr>

            <c:forEach var="feature" items="${formBean.genomicFeatures}" varStatus="loop">
                <tr class=${loop.index%2==0 ? "even" : "odd"}>
                <td>
                    <zfin:link entity="${feature}"/>
                </td>
                <td>
                    <zfin:link entity="${feature.affectedGenes}"/>
                </td>
                <td>
                    &nbsp;
                </td>
                <td>
                    &nbsp;
                </td>
            </tr>
        </c:forEach>
    </table>
</zfin2:subsection>
</div>
        </c:if>

<%--// EXPRESSION --%>
<div class="summary" id="expression">
<b>GENE EXPRESSION</b>
<a class="popup-link info-popup-link" href="/ZFIN/help_files/expression_help.html"></a>
<br/>
<b>Gene expression in Wild Types + ${formBean.marker.name}</b>
<c:choose>
    <c:when test="${formBean.expressionDisplays != null && fn:length(formBean.expressionDisplays) > 0 }">
        <zfin2:expressionData sequenceTargetingReagentID="${sequenceTargetingReagent.zdbID}"
                              expressionDisplays="${formBean.expressionDisplays}" showCondition="false"/>
    </c:when>
    <c:otherwise>
        <span class="no-data-tag">No data available</span>
    </c:otherwise>
</c:choose>
</div>

<%--// PHENOTYPE --%>
<div class="summary">
<b>PHENOTYPE</b>
<a class='popup-link info-popup-link' href='/action/marker/note/phenotype'></a>
<br/>
<c:choose>
    <c:when test="${formBean.phenoMartBeingRegened}">
        <div>
            <img src="/images/warning-noborder.gif" alt="transcript withdrawn" width="20" height="20" align="top"
                 class="blast-key"/>
            Data in this section is temporarily unavailable, please reload in a few minutes.
        </div>
    </c:when>
    <c:otherwise>
        <b>Phenotype resulting from ${formBean.marker.name}</b>
        <div id="phenotype">
            <c:choose>
                <c:when test="${formBean.phenotypeDisplays != null && fn:length(formBean.phenotypeDisplays) > 0 }">
                    <zfin2:all-phenotype phenotypeDisplays="${formBean.phenotypeDisplays}" suppressMoDetails="true"
                                         fishAndCondition="false" secondColumn="fish"/>
                </c:when>
                <c:otherwise>
                    <span class="no-data-tag">No data available</span>
                </c:otherwise>
            </c:choose>
        </div>
        <br/>
        <b>Phenotype of all Fish created by or utilizing ${formBean.marker.name}</b>
        <div id="allPhenotype">
            <c:choose>
                <c:when test="${formBean.allPhenotypeDisplays != null && fn:length(formBean.allPhenotypeDisplays) > 0 }">
                    <zfin2:all-phenotype phenotypeDisplays="${formBean.allPhenotypeDisplays}"
                                         suppressMoDetails="true" fishAndCondition="true" secondColumn="fish"/>
                </c:when>
                <c:otherwise>
                    <span class="no-data-tag">No data available</span>
                </c:otherwise>
            </c:choose>
        </div>
    </c:otherwise>
</c:choose>
</div>

<%--OTHER GENE/Marker Pages--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<script>
jQuery(function () {
    jQuery('#expression').tableCollapse({label: 'expressed genes'});
    jQuery('#phenotype').tableCollapse({label: 'phenotypes'});
    jQuery('#allPhenotype').tableCollapse({label: 'phenotypes'});
    jQuery('#genomicFeature').tableCollapse({label: 'features'});
});
</script>
