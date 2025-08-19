<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="geneDescription" type="org.zfin.marker.AllianceGeneDesc" %>

<z:attributeListItem>
    <jsp:attribute name="label">
        Description <a class='popup-link info-popup-link' href='/action/marker/note/automated-gene-desc'></a>
    </jsp:attribute>
    <jsp:body>
        <z:ifHasData test="${!empty geneDescription && !empty geneDescription.gdDesc && geneDescription.gdDesc != 'null'}" noDataMessage="No data available">
            ${geneDescription.gdDesc}
        </z:ifHasData>
    </jsp:body>
</z:attributeListItem>
