<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="markerExpression" type="org.zfin.expression.presentation.MarkerExpression" rtexprvalue="true"
              required="true" %>
<%@ attribute name="webdriverRoot" type="java.lang.String" rtexprvalue="true" required="true" %>

<script type="text/javascript">
    function start_note(ref_page) {
        top.zfinhelp = open("<%=ZfinProperties.getWebDriver()%>?MIval=aa-" + ref_page + ".apg", "notewindow", "scrollbars=no,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=325");
    }

</script>


<!--todo: need to cound the # of expression results, should be greater than 0-->
<%--N cases:--%>
<%--1 - (lots of stuff available): gt 1  || (markerExpression.geoLinkSearching = false && gt 0)--%>
<%--2 - (lots of stuff available but still searching) gt 1 && || (markerExpression.geoLinkSearching) gt 1:--%>
<%--[put No data avialable / or blank into Curated Microarray Expression]--%>
<%--3 - (only geo link is possible) eq 1 && (markerExpression.geoLinkSearching):--%>
<%--[put No data avialable / or blank into Curated Microarray Expression]--%>
<%--4 - (no data available) (markerExpression.geoLinkSearching = false && eq 0)--%>
<div class="summary">
    <c:choose>
        <c:when test="${markerExpression.totalCountForStuff gt 0}">

            <table id="geneExpressionData" class="summary horizontal-solidblock geneexpressionblock">
                <caption>GENE EXPRESSION
                    <small><a class="popup-link info-popup-link" href="/action/marker/note/expression"></a></small>
                </caption>
                <c:if test="${
                (!empty markerExpression.allExpressionData and empty markerExpression.directlySubmittedExpression)
                or
                (markerExpression.allExpressionData.figureCount > markerExpression.directlySubmittedExpression.figureCount)
                }">
                    <tr>
                        <td class="data-label"><b>All Expression Data: </b></td>
                        <td align="left">
                            <c:choose>
                            <c:when test="${markerExpression.allExpressionData.figureCount eq 1}">
                                ${markerExpression.allExpressionData.singleFigure.link}
                            </c:when>
                            <c:otherwise>
                            <a href="/${webdriverRoot}?MIval=aa-xpatselect.apg&query_results=true&gene_name=${marker.abbreviation}&searchtype=equals"
                                    >${markerExpression.allExpressionData.figureCount}
                                figures
                                </c:otherwise>
                                </c:choose>
                            </a> from
                            <c:choose>
                                <c:when test="${markerExpression.allExpressionData.publicationCount eq 1}">
                                    <zfin:link entity="${markerExpression.allExpressionData.singlePublication}"/>
                                </c:when>
                                <c:otherwise>
                                    ${markerExpression.allExpressionData.publicationCount} publications
                                </c:otherwise>
                            </c:choose>
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
                                <a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-fxallfigures.apg&OID=${directlySubmittedExpression.publicationZdbID}&fxallfig_probe_zdb_id=${directlySubmittedExpression.probeFeatureZdbId}">
                                        ${directlySubmittedExpression.numFigures}
                                    figure${directlySubmittedExpression.numFigures ne 1 ? 's' : ''}
                                    (${directlySubmittedExpression.numImages}
                                    image${directlySubmittedExpression.numImages ne 1 ? 's' : ''})
                                </a>
                                from <a
                                    href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${directlySubmittedExpression.publicationZdbID}">${directlySubmittedExpression.miniAuth}</a>
                                <c:if test="${marker.markerType.type != 'EFG'  }">
                                    [${directlySubmittedExpression.probeFeatureAbbrev}]
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>


                <c:if test="${!empty markerExpression.wildTypeStageExpression.expressedStructures}">
                    <tr>
                        <th class="data-label"><b>Wild-type Stages, Structures:</b></th>
                        <td align="left">
                            <zfin:link
                                    entity="${markerExpression.wildTypeStageExpression.expressionPresentation.startStage}"
                                    longVersion="true"/>
                            to
                            <zfin:link
                                    entity="${markerExpression.wildTypeStageExpression.expressionPresentation.endStage}"
                                    longVersion="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <zfin2:toggledPostcomposedList
                                    expressionResults="${markerExpression.wildTypeStageExpression.expressedStructures}"
                                    showAttributionLinks="false"
                                    maxNumber="4"
                                    />
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
                <b>GENE EXPRESSION
                    <small><a class="popup-link info-popup-link" href="/action/marker/note/expression"></a></small>
                </b>
                <zfin2:noDataAvailable/>
            </div>
        </c:otherwise>
    </c:choose>
</div>

