<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>
<c:set var="phenotypeOnMarkerBean" value="${formBean.phenotypeOnMarkerBeans}"/>

<c:set var="hasData" value="${!empty phenotypeOnMarkerBean and phenotypeOnMarkerBean.numPublications > 0}" />
<c:set var="allianceUrl">https://alliancegenome.org/gene/ZFIN:${formBean.marker.zdbID}#phenotypes</c:set>

<z:attributeList>
    <z:attributeListItem label="All Phenotype Data">
        <z:ifHasData test="${hasData}">
            <zfin2:markerPhenotypeLink phenotypeOnMarkerBean="${phenotypeOnMarkerBean}" marker="${formBean.marker}" />
        </z:ifHasData>
    </z:attributeListItem>

    <z:attributeListItem label="Cross-Species Comparison">
        <zfin2:externalLink href="${allianceUrl}">Alliance</zfin2:externalLink>
    </z:attributeListItem>
</z:attributeList>

