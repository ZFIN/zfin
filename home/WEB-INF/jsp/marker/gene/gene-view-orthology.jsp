<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request" />

<c:if test="${!empty geneTree}">
    <c:set var="ensemblUrl">https://ensembl.org/Danio_rerio/Gene/Compara_Tree?g=${geneTree}</c:set>
</c:if>

<z:attributeList>



    <z:attributeListItem label="Comparative Orthology">

                <zfin2:externalLink href="${allianceUrl}">Alliance</zfin2:externalLink>

    </z:attributeListItem>
    <c:if test="${!empty ensemblUrl}">
        <z:attributeListItem label="Gene Tree">
            <zfin2:externalLink href="${ensemblUrl}">Ensembl</zfin2:externalLink>
        </z:attributeListItem>
    </c:if>
    <c:if test="${!empty orthologyNote}">
        <z:attributeListItem label="Note">
            ${orthologyNote}
        </z:attributeListItem>
    </c:if>
</z:attributeList>

<div class="__react-root" id="OrthologyTable" data-gene-id="${formBean.marker.zdbID}"></div>
