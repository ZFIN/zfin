<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hit" type="org.zfin.sequence.blast.results.view.HitViewBean" rtexprvalue="true" required="true" %>

<c:choose>
    <c:when test="${hit.hasExpressionImages}">
        <a href="/<%=org.zfin.properties.ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect.apg&query_results=true&xpatsel_geneZdbId=${hit.gene.zdbID}&gene_name=${hit.gene.abbreviation}" ><img src="/images/E_camera.png" title="view gene expression" alt="has expression figures" border="0" align="top" class="blast"></a>
    </c:when>
    <c:when test="${hit.hasExpression}">
        <a href="/<%=org.zfin.properties.ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect.apg&query_results=true&xpatsel_geneZdbId=${hit.gene.zdbID}&gene_name=${hit.gene.abbreviation}"><img src="/images/E_letter.png" title="view gene expression" alt="has expression" border="0" align="top" class="blast"></a>
    </c:when>
</c:choose>

<c:if test="${hit.hasGO}">
    <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-markergoview.apg&OID=${hit.gene.zdbID}"><img src="/images/G_letter.png" title="view GO annotation" alt="has GO annotation" border="0" class="blast"></a>
</c:if>

<c:choose>
    <c:when test="${hit.hasPhenotypeImages}">
        <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${hit.gene.zdbID}"><img src="/images/P_camera.png" title="view phenotype data" alt="has phenotype annotation" border="0" class="blast"></a>
    </c:when>
    <c:when test="${hit.hasPhenotype}">
        <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${hit.gene.zdbID}"><img src="/images/P_letter.png" title="view phenotype data" alt="has phenotype annotation" border="0" class="blast"></a>
    </c:when>
</c:choose>
