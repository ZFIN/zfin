<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<c:if test="${!empty geneTree}">
<c:set var="ensemblUrl">https://ensembl.org/Danio_rerio/Gene/Compara_Tree?g=${geneTree}</c:set>
</c:if>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>
<c:if test="${!empty ensemblUrl}">
<zfin2:externalLink href="${ensemblUrl}">Ensembl Gene Tree</zfin2:externalLink>
</c:if>
<div class="__react-root" id="OrthologyTable" data-gene-id="${formBean.marker.zdbID}"></div>
<c:if test="${!empty orthologyNote}">
    <div>
        <b>Orthology Note</b>
        <div>${orthologyNote}</div>
    </div>
</c:if>
