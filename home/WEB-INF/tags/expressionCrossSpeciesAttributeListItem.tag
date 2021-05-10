<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" required="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="bgeeIdList" required="false" rtexprvalue="true" type="java.lang.String" %>

<c:set var="allianceUrl">https://alliancegenome.org/gene/ZFIN:${marker.zdbID}#expression</c:set>
<c:set var="bGeeUrl">https://bgee.org/?page=expression_comparison&gene_list=${bgeeIdList}</c:set>

<z:attributeListItem label="Cross-Species Comparison">
    <ul class="list-inline m-0">
        <li class="list-inline-item">
            <zfin2:externalLink href="${allianceUrl}">Alliance</zfin2:externalLink>
        </li>
        <c:if test="${!empty bgeeIdList}">
            <li class="list-inline-item">
                <zfin2:externalLink href="${bGeeUrl}">Bgee</zfin2:externalLink>
            </li>
        </c:if>
    </ul>
</z:attributeListItem>
