<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="markerExpression" type="org.zfin.expression.presentation.MarkerExpression" rtexprvalue="true"
              required="true" %>

<!--todo: need to cound the # of expression results, should be greater than 0-->
<%--N cases:--%>
<%--1 - (lots of stuff available): gt 1  || (markerExpression.geoLinkSearching = false && gt 0)--%>
<%--2 - (lots of stuff available but still searching) gt 1 && || (markerExpression.geoLinkSearching) gt 1:--%>
<%--[put No data avialable / or blank into Curated Microarray Expression]--%>
<%--3 - (only geo link is possible) eq 1 && (markerExpression.geoLinkSearching):--%>
<%--[put No data avialable / or blank into Curated Microarray Expression]--%>
<%--4 - (no data available) (markerExpression.geoLinkSearching = false && eq 0)--%>
<z:attributeList>
<c:choose>
    <c:when test="${markerExpression.totalCountForStuff gt 0}">
                <c:if test="${markerExpression.directlySubmittedExpression ne null}">
                    <z:attributeListItem label="Directly Submitted Expression Data">

                    <c:forEach var="directlySubmittedExpression"
                               items="${markerExpression.directlySubmittedExpression.markerExpressionInstances}"
                               varStatus="index">



                                <a href="/action/publication/${directlySubmittedExpression.publicationZdbID}/all-figures?probeZdbID=${directlySubmittedExpression.probeFeatureZdbId}">
                                        ${directlySubmittedExpression.numFigures}
                                    figure${directlySubmittedExpression.numFigures ne 1 ? 's' : ''}
                                    (${directlySubmittedExpression.numImages}
                                    image${directlySubmittedExpression.numImages ne 1 ? 's' : ''})
                                </a>
                                from <a
                                    href="/${directlySubmittedExpression.publicationZdbID}">${directlySubmittedExpression.miniAuth}</a>
                                <c:if test="${marker.markerType.type != 'EFG'  }">
                                    [${directlySubmittedExpression.probeFeatureAbbrev}]
                                </c:if>


                    </c:forEach>

                    </z:attributeListItem>
                </c:if>



             <c:if test="${markerExpression.geoLink !=null}">
                 <z:attributeListItem label="High Throughput Expression">
                       ${markerExpression.geoLink}
                                <c:if test="${markerExpression.expressionAtlasLink.link != null}">, <a href="${markerExpression.expressionAtlasLink.link}">Expression Atlas</a>${markerExpression.expressionAtlasLink.attributionLink}
                                </c:if>
                 </z:attributeListItem>
                </c:if>


    </c:when>
    <c:otherwise>

        <%--<z:attributeListItem label="">--%>
        <div class="col-sm-2 color: #6c757d">


            <i>No data available</i>
            </div>
        <%--</z:attributeListItem>--%>

    </c:otherwise>
</c:choose>

</z:attributeList>