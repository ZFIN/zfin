<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="geneBean" type="org.zfin.marker.presentation.GeneBean" %>

<div class="summary">
    <b>GENE PRODUCT DESCRIPTION</b>
<c:choose>
    <c:when test="${fn:length(geneBean.geneProductsBean)>0}">
            <a class="popup-link data-popup-link" href="/action/marker/gene-product-description/${geneBean.marker.zdbID}"></a>
    </c:when>
    <c:otherwise>
        <span class="no-data-tag">No description available</span>
    </c:otherwise>
</c:choose>
</div>

