<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="criteria" type="org.zfin.search.presentation.MarkerSearchCriteria"%>

<table class="searchresults">
    <caption>${criteria.numFound} results</caption>
    <tr>
        <th>Symbol - Name</th>
        <th>Expression</th>
        <th>Phenotype</th>
        <th>Location</th>
        <th>Matching Text</th>
    </tr>
    <c:forEach var="result" items="${criteria.results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${criteria.results}" groupByBean="marker.zdbID">
            <td>
                <zfin:link entity="${result.marker}"/> - ${result.marker.name}
            </td>
            <td>
                <c:if test="${
                (!empty result.markerExpression.allExpressionData
                  and empty result.markerExpression.directlySubmittedExpression)
                or
                (result.markerExpression.allExpressionData.figureCount > result.markerExpression.directlySubmittedExpression.figureCount)
                }">
                <zfin2:expressionLink marker="${result.marker}" markerExpression="${result.markerExpression}"/>
                </c:if>
            </td>
            <td>
                <c:if test="${!empty result.markerPhenotype
                               and result.markerPhenotype.numPublications > 0}">
                    <zfin2:markerPhenotypeLink phenotypeOnMarkerBean="${result.markerPhenotype}" marker="${result.marker}"/>
                </c:if>
            </td>
            <td>
                <zfin2:displayLocation entity="${result.marker}"/>
            </td>
            <td>
                    ${result.matchingText}
                    <c:if test="${!empty result.score}">
                        <c:out value="${result.score}"/>
                    </c:if>
                    <c:if test="${!empty result.explain}">
                        <div style="display:none">
                            <c:out value="${result.explain}"/>
                        </div>
                    </c:if>
            </td>
        </zfin:alternating-tr>
    </c:forEach>

</table>