<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="markerExpression" required="true" type="org.zfin.expression.presentation.MarkerExpression" %>

<c:set var="hasGeo" value="${!empty markerExpression.geoLink}"/>
<c:set var="hasAtlas" value="${!empty markerExpression.expressionAtlasLink.link}"/>
<c:set var="hasSingleCellAtlas" value="${!empty markerExpression.singleCellExpressionAtlasLink}"/>
<c:set var="hasData" value="${hasGeo or hasAtlas}"/>

<z:attributeListItem label="High Throughput Data">
    <z:ifHasData test="${hasData}">
        <ul class="comma-separated list-inline m-0">
            <c:if test="${hasGeo}">
                <li class="list-inline-item">${markerExpression.geoLink}</li>
            </c:if>
            <c:if test="${hasAtlas}">
                <zfin2:externalLink
                        href="${markerExpression.expressionAtlasLink.link}">Expression Atlas</zfin2:externalLink>
                ${markerExpression.expressionAtlasLink.attributionLink}
            </c:if>
            <c:if test="${!empty markerExpression.ensdargGenes}">
                <c:forEach var="ensdarg" items="${markerExpression.ensdargGenes}" varStatus="loop">
                    <zfin2:externalLink
                            href="https://cells.ucsc.edu/?ds=zebrafish-dev&gene=${ensdarg}">UO scRNA-seq at UCSC browser</zfin2:externalLink>
                    (<a href="/ZDB-PUB-210917-2">1</a>)
                </c:forEach>
            </c:if>
            <c:if test="${hasSingleCellAtlas}">
                <zfin2:externalLink
                        href="${markerExpression.singleCellExpressionAtlasLink}">Single Cell Expression Atlas</zfin2:externalLink>
                (<a href="/ZDB-PUB-220103-3">1</a>)
            </c:if>
            <c:if test="${!empty markerExpression.fishMiRnaLink}">
                <zfin2:externalLink
                        href="http://fishmirna.org/index.html?fishmirna_mature_id=${markerExpression.fishMiRnaLink.accNumDisplay}">
                    FishMiRNA</zfin2:externalLink>
                (<a href="/ZDB-PUB-220126-55">1</a>)
            </c:if>
        </ul>
    </z:ifHasData>
</z:attributeListItem>