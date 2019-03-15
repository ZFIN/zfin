<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="markerExpression" type="org.zfin.expression.presentation.MarkerExpression" rtexprvalue="true"
              required="true" %>
<%@ attribute name="webdriverRoot" type="java.lang.String" rtexprvalue="true" required="true" %>

<!--todo: need to cound the # of expression results, should be greater than 0-->
<%--N cases:--%>
<%--1 - (lots of stuff available): gt 1  || (markerExpression.geoLinkSearching = false && gt 0)--%>
<%--2 - (lots of stuff available but still searching) gt 1 && || (markerExpression.geoLinkSearching) gt 1:--%>
<%--[put No data avialable / or blank into Curated Microarray Expression]--%>
<%--3 - (only geo link is possible) eq 1 && (markerExpression.geoLinkSearching):--%>
<%--[put No data avialable / or blank into Curated Microarray Expression]--%>
<%--4 - (no data available) (markerExpression.geoLinkSearching = false && eq 0)--%>
<a name="gene_expression"></a>

<div class="summary">
    <c:choose>
        <c:when test="${markerExpression.totalCountForStuff gt 0}">

            <table id="geneExpressionData" class="summary horizontal-solidblock geneexpressionblock">
                <caption>
                    GENE EXPRESSION
                    <a class="popup-link info-popup-link" href="/ZFIN/help_files/expression_help.html"></a>
                </caption>
                <c:if test="${
                (!empty markerExpression.allExpressionData and empty markerExpression.directlySubmittedExpression)
                or
                (markerExpression.allExpressionData.figureCount > markerExpression.directlySubmittedExpression.figureCount)
                }">
                    <tr>
                        <td class="data-label"><b>All Expression Data: </b></td>
                        <td align="left">
                            <zfin2:expressionLink marker="${marker}" markerExpression="${markerExpression}"/>
                        </td>
                    </tr>
                </c:if>

                <c:if test="${markerExpression.directlySubmittedExpression ne null}">
                    <c:forEach var="directlySubmittedExpression"
                               items="${markerExpression.directlySubmittedExpression.markerExpressionInstances}"
                               varStatus="index">
                        <tr>
                            <c:choose>
                                <c:when test="${index.first}">
                                    <%--<th>Directly submitted expression data:</th>--%>
                                    <td class="data-label"><b>Directly Submitted Expression Data:</b></td>
                                </c:when>
                                <c:otherwise>
                                    <td> &nbsp;</td>
                                </c:otherwise>
                            </c:choose>
                            <td align="left">
                                <a href="/action/figure/all-figure-view/${directlySubmittedExpression.publicationZdbID}?probeZdbID=${directlySubmittedExpression.probeFeatureZdbId}">
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
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>


                <c:if test="${!empty markerExpression.wildTypeStageExpression.expressedStructures}">
                    <tr>
                        <td class="data-label"><b>Wild-type Stages, Structures:</b></td>
                        <td align="left">
                            <zfin:link
                                    entity="${markerExpression.wildTypeStageExpression.expressionPresentation.startStage}"
                                    longVersion="true"/>
                            <c:if test="${not empty markerExpression.wildTypeStageExpression.expressionPresentation.endStage }">
                                to
                                <zfin:link
                                        entity="${markerExpression.wildTypeStageExpression.expressionPresentation.endStage}"
                                        longVersion="true"/>
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <zfin2:toggledLinkList commaDelimited="true"
                                                   collection="${markerExpression.wildTypeStageExpression.expressedStructures}"
                                                   maxNumber="4" />
                        </td>
                    </tr>
                </c:if>
                <c:if test="${markerExpression.geoLink !=null}">
                    <tr>
                        <td class="data-label"><b>Curated Microarray Expression:</b></td>
                        <td align="left">
                                ${markerExpression.geoLink}
                        </td>
                    </tr>
                </c:if>
            </table>
        </c:when>
        <c:otherwise>
            <div class="noGeneExpressionData">
                <b>
                    GENE EXPRESSION
                    <a class="popup-link info-popup-link" href="/ZFIN/help_files/expression_help.html"></a>
                </b>
                <zfin2:noDataAvailable/>
            </div>
        </c:otherwise>
    </c:choose>
</div>

