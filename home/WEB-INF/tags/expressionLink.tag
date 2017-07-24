<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="marker" type="org.zfin.marker.Marker"%>
<%@attribute name="markerExpression" type="org.zfin.expression.presentation.MarkerExpression"%>

<c:choose>
    <c:when test="${markerExpression.allExpressionData.figureCount == 1}">

        ${markerExpression.allExpressionData.singleFigure.link}

    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${marker.markerType.type != 'EFG'  }">
                <a href="/cgi-bin/webdriver?MIval=aa-xpatselect.apg&query_results=true&gene_name=${marker.abbreviation}&searchtype=equals"
                >${markerExpression.allExpressionData.figureCount}
                figures</a>
            </c:when>
            <c:otherwise>
                <a href="/cgi-bin/webdriver?MIval=aa-xpatselect.apg&query_results=true&gene_name=${marker.name}&searchtype=equals"
                >${markerExpression.allExpressionData.figureCount} figures</a>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
 from
<c:choose>
    <c:when test="${markerExpression.allExpressionData.publicationCount eq 1}">
        <zfin:link entity="${markerExpression.allExpressionData.singlePublication}"/>
    </c:when>
    <c:otherwise>
        ${markerExpression.allExpressionData.publicationCount} publications
    </c:otherwise>
</c:choose>


<authz:authorize access="hasRole('root')">
    <a class="small" style="padding-right: 50px;" href="/action/marker/${marker.zdbID}/expression">(New Expression Search)</a>
</authz:authorize>