<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figureExpressionSummaryList" type="java.util.List" required="true"
              description="List of FigureSummaryDisplay objects" %>

<%@ attribute name="expressionData" type="java.lang.Boolean" required="false" %>

<c:if test="${!empty figureExpressionSummaryList}">

    <table class="summary groupstripes">
        <tr>
            <th align="left" width="20%">Publication</th>
            <th align="left" width="5%">Data</th>
            <th align="left" width="5%"> &nbsp; </th>
            <c:if test="${expressionData}">
                <th align="left" width="15%">Expressed Genes</th>
            </c:if>
            <th align="left">
                <c:choose>
                    <c:when test="${expressionData}">
                        Anatomy
                    </c:when>
                    <c:otherwise>
                        Phenotype </c:otherwise>
                </c:choose>
            </th>
        </tr>
        <c:forEach var="figureExpressionSummaryDisplay" items="${figureExpressionSummaryList}" varStatus="status">
            <zfin:alternating-tr loopName="status"
                                 groupBeanCollection="${figureExpressionSummaryList}"
                                 groupByBean="publication">
                <td>
                    <zfin:groupByDisplay loopName="status"
                                         groupBeanCollection="${figureExpressionSummaryList}"
                                         groupByBean="publication">
                        <zfin:link entity="${figureExpressionSummaryDisplay.figure.publication}"/>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    <zfin:groupByDisplay loopName="status"
                                         groupBeanCollection="${figureExpressionSummaryList}"
                                         groupByBean="figure">
                        <zfin:link entity="${figureExpressionSummaryDisplay.figure}"/>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    <zfin:groupByDisplay loopName="status"
                                         groupBeanCollection="${figureExpressionSummaryList}"
                                         groupByBean="figure">
                        <c:if test="${figureExpressionSummaryDisplay.thumbnail != null}">
                            <zfin:link entity="${figureExpressionSummaryDisplay.figure}">
                                <img border="1" src="/imageLoadUp/${figureExpressionSummaryDisplay.thumbnail}"
                                     height="50"
                                     title="${figureExpressionSummaryDisplay.imgCount} image<c:if test="${figureExpressionSummaryDisplay.imgCount > 1}">s</c:if>"
                                     alt=""/>
                                <c:if test="${figureExpressionSummaryDisplay.imgCount > 1}">
                                    <img border="0" src="/images/multibars.gif" alt="multiple images"/>
                                </c:if>
                            </zfin:link>
                        </c:if>
                    </zfin:groupByDisplay>
                </td>
                <c:if test="${expressionData}">
                    <td>
                        <zfin:link entity="${figureExpressionSummaryDisplay.expressedGene.gene}"/>
                    </td>
                </c:if>
                <td>
                    <c:choose>
                        <c:when test="${expressionData}">
                            <zfin2:toggledHyperlinkList
                                    collection="${figureExpressionSummaryDisplay.expressedGene.expressionStatements}"
                                    maxNumber="6" id="${figureExpressionSummaryDisplay.figure.zdbID}-terms"/>
                        </c:when>
                        <c:otherwise>
                            <zfin2:toggledHyperlinkList
                                    collection="${figureExpressionSummaryDisplay.phenotypeStatementList}"
                                    maxNumber="6" id="${figureExpressionSummaryDisplay.figure.zdbID}-terms"
                                    commaDelimited="false"/>
                        </c:otherwise>
                    </c:choose>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:if>
