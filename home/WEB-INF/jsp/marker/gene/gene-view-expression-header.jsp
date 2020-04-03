<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>
<c:set var="markerExpression" value="${formBean.markerExpression}"/>

<z:attributeList>
    <z:attributeListItem label="All Expression Data:">
        <c:if test="${
                (!empty markerExpression.allExpressionData and empty markerExpression.directlySubmittedExpression)
                or
                (markerExpression.allExpressionData.figureCount > markerExpression.directlySubmittedExpression.figureCount)
                }"><zfin2:expressionLink marker="${formBean.marker}" markerExpression="${markerExpression}"/>
        </c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Cross-Species Comparison:">
        <a href="https://alliancegenome.org/gene/ZFIN:${formBean.marker.zdbID}">Alliance</a>
    </z:attributeListItem>

    <z:attributeListItem label="High Throughput Data:">
        <c:if test="${markerExpression.geoLink !=null}">
            ${markerExpression.geoLink}
            <c:if test="${markerExpression.expressionAtlasLink.link != null}">, <a
                    href="${markerExpression.expressionAtlasLink.link}">Expression
                Atlas</a>${markerExpression.expressionAtlasLink.attributionLink}
            </c:if>
        </c:if>
    </z:attributeListItem>

</z:attributeList>