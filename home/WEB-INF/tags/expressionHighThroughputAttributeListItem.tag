<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="markerExpression" required="true" type="org.zfin.expression.presentation.MarkerExpression" %>

<c:set var="hasGeo" value="${!empty markerExpression.geoLink}" />
<c:set var="hasAtlas" value="${!empty markerExpression.expressionAtlasLink.link}" />
<c:set var="hasData" value="${hasGeo or hasAtlas}" />

<z:attributeListItem label="High Throughput Data">
    <z:ifHasData test="${hasData}">
        <ul class="list-inline m-0">
            <c:if test="${hasGeo}">
                <li class="list-inline-item">${markerExpression.geoLink}</li>
            </c:if>
            <c:if test="${hasAtlas}">
                <zfin2:externalLink href="${markerExpression.expressionAtlasLink.link}">Expression Atlas</zfin2:externalLink>
                ${markerExpression.expressionAtlasLink.attributionLink}
            </c:if>
        </ul>
    </z:ifHasData>
</z:attributeListItem>