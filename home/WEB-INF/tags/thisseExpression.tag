<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="markerExpression" required="true" type="org.zfin.expression.presentation.MarkerExpression" %>


<c:set var="hasData" value="${markerExpression.directlySubmittedExpression ne null}"/>


<z:attributeListItem label="Thisse Expression Data">
    <z:ifHasData test="${hasData}">
        <ul class="comma-separated">
            <c:forEach var="directlySubmittedExpression"
                       items="${markerExpression.directlySubmittedExpression.markerExpressionInstances}"
                       varStatus="index">
                <li>
                    <c:if test="${marker.markerType.type != 'EFG'  }">
                            ${directlySubmittedExpression.probeFeatureAbbrev}
                    </c:if>
                    <a href="/action/figure/all-figure-view/${directlySubmittedExpression.publicationZdbID}?probeZdbID=${directlySubmittedExpression.probeFeatureZdbId}">
                        (${directlySubmittedExpression.numImages}
                        image${directlySubmittedExpression.numImages ne 1 ? 's' : ''})
                    </a>
                </li>
            </c:forEach>
        </ul>
    </z:ifHasData>
</z:attributeListItem>