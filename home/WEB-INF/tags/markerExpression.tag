<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="markerExpression" type="org.zfin.expression.presentation.MarkerExpression" rtexprvalue="true" required="true" %>

<script type="text/javascript">
    function start_note(ref_page) {
        top.zfinhelp=open("<%=ZfinProperties.getWebDriver()%>?MIval=aa-"+ref_page+".apg","notewindow","scrollbars=no,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=325");
    }

</script>


<!--todo: need to cound the # of expression results, should be greater than 0-->
<c:if test="${markerExpression.totalCountForStuff gt 0}">

    <hr width="80%"/>
    <div class="summary">
    <table class="summary solidblock geneexpressionblock">
        <caption>GENE EXPRESSION: <small>(<a href="javascript:start_note('xpatselect_note')">current status</a>)</small>  </caption>
        <c:if test="${markerExpression.allExpressionData.totalCount > 0}">
            <tr>
                <td>All expression data:  <a href="">${markerExpression.allExpressionData.figureCount} figures</a> from ${markerExpression.allExpressionData.publicationCount} pubs</td>
            </tr>
        </c:if>

        <c:if test="${markerExpression.directlySubmittedExpression ne null}">
            <c:forEach var="directlySubmittedExpression" items="${markerExpression.directlySubmittedExpression.expressionSummaryInstances}" varStatus="index">
                <tr>
                    <c:choose>
                        <c:when test="${index.first}">
                            <td> <b>Directly submitted expression data:</b></td>
                        </c:when>
                        <c:otherwise>
                            <td> &nbsp;</td>
                        </c:otherwise>
                    </c:choose>
                    <td align="right">
                        <a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-fxallfigures.apg&OID=${directlySubmittedExpression.singlePublication.zdbID}&fxallfig_probe_zdb_id=${directlySubmittedExpression.marker.zdbID}">
                                ${directlySubmittedExpression.figureCount}
                            figure${directlySubmittedExpression.figureCount gt 0 ? 's' : ''}
                            (${directlySubmittedExpression.imageCount}
                            image${directlySubmittedExpression.imageCount gt 0 ? 's' : ''})
                        </a>
                        from ${directlySubmittedExpression.singlePublication.shortAuthorList} ${directlySubmittedExpression.marker.abbreviation}
                    </td>
                </tr>
            </c:forEach>
        </c:if>


        <c:if test="${markerExpression.wildTypeStageExpression ne null}">
            <tr>
                <td></td>
            </tr>
        </c:if>

        <c:if test="${markerExpression.microarrayLinks ne null}">
            <tr>
                <td>
                    <c:forEach var="microarrayLink" items="${markerExpression.microarrayLinks}">
                        <zfin:link entity="${microarrayLink}"/>
                        <zfin:attribution entity="${microarrayLink}"/>
                    </c:forEach>
                </td>
            </tr>
        </c:if>
    </table>
    </div>
</c:if>



