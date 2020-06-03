<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>
<c:set var="phenotypeOnMarkerBean" value="${formBean.phenotypeOnMarkerBeans}"/>

<z:attributeList>
    <z:attributeListItem label="All Phenotype Data:">
        <c:if test="${!empty phenotypeOnMarkerBean and phenotypeOnMarkerBean.numPublications>0}">
                        <zfin2:markerPhenotypeLink phenotypeOnMarkerBean="${phenotypeOnMarkerBean}" marker="${formBean.marker}"/>
        </c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Cross-Species Comparison:">
        <zfin2:externalLink href="https://alliancegenome.org/gene/ZFIN:${formBean.marker.zdbID}#phenotypes">Alliance
        </zfin2:externalLink>
    </z:attributeListItem>
</z:attributeList>

