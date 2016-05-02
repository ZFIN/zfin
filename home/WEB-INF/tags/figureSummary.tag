<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figureSummaryList" type="java.util.List" required="true"
              description="List of FigureSummaryDisplay objects" %>

<%@ attribute name="expressionData" type="java.lang.Boolean" required="false" %>
<%@ attribute name="phenotypeData" type="java.lang.Boolean" required="false" %>
<%@ attribute name="expressionGenotypeData" type="java.lang.Boolean" required="false" %>
<%@ attribute name="showMarker" type="java.lang.Boolean" required="false" %>
<%@ attribute name="showGenotype" type="java.lang.Boolean" required="false" %>

<c:if test="${!empty figureSummaryList}">

    <table class="summary rowstripes">
        <tr>
            <th align="left" width="20%">Publication</th>
            <th align="left" width="5%">Data</th>
            <th align="left" width="300"> &nbsp; </th>
            <c:if test="${showGenotype}">
                <%--<th align="left" width="15%">Conditions</th>--%>
                <th align="left" width="15%">Fish</th>
            </c:if>
                <%--<th align="left" width="5%"> &nbsp; </th>--%>
            <c:if test="${showMarker}">
                <th align="left" width="15%">Expressed Genes</th>
            </c:if>
            <th align="left">
                Anatomy
            </th>
        </tr>
        <c:forEach var="figureExpressionSummaryDisplay" items="${figureSummaryList}" varStatus="status">
            <zfin:alternating-tr loopName="status"
                                 groupBeanCollection="${figureSummaryList}"
                                 groupByBean="figure">
                <td>
                    <zfin:groupByDisplay loopName="status"
                                         groupBeanCollection="${figureSummaryList}"
                                         groupByBean="publication">
                        <zfin:link entity="${figureExpressionSummaryDisplay.figure.publication}"/>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    <zfin:groupByDisplay loopName="status"
                                         groupBeanCollection="${figureSummaryList}"
                                         groupByBean="figure">
                        <zfin:link entity="${figureExpressionSummaryDisplay.figure}"/>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    <zfin:groupByDisplay loopName="status"
                                         groupBeanCollection="${figureSummaryList}"
                                         groupByBean="figure">
                        <c:if test="${figureExpressionSummaryDisplay.thumbnail != null}">
                            <zfin:link entity="${figureExpressionSummaryDisplay.figure}">
                                <img border="1" src="/imageLoadUp/${figureExpressionSummaryDisplay.thumbnail}"
                                     height="50"
                                     title="${figureExpressionSummaryDisplay.imgCount} image<c:if test="${figureExpressionSummaryDisplay.imgCount > 1}">s</c:if>"
                                     alt=""
                                     style="margin:0px -4px"/>
                                <c:if test="${figureExpressionSummaryDisplay.imgCount > 1}">
                                    <img border="0" src="/images/multibars.gif" alt="multiple images"/>
                                </c:if>
                            </zfin:link>
                        </c:if>
                    </zfin:groupByDisplay>
                </td>
                <c:if test="${showGenotype}">
                    <td>
                    <c:if test="${!empty figureExpressionSummaryDisplay.fishList}">
                        <c:forEach var="fish" items="${figureExpressionSummaryDisplay.fishList}" varStatus="status">
                            <zfin:link entity="${figureExpressionSummaryDisplay.fishList[status.index]}"/>


                            <c:if test="${!status.last}">,&nbsp&nbsp;</c:if>
                        </c:forEach>

                        </td>
                    </c:if>
                </c:if>
                <c:if test="${showMarker}">
                    <td>
                        <zfin:link entity="${figureExpressionSummaryDisplay.expressedGene.gene}"/>
                    </td>
                </c:if>

                <td>
                    <c:if test="${expressionData}">
                        <zfin2:toggledHyperlinkList
                                collection="${figureExpressionSummaryDisplay.expressedGene.expressionStatements}"
                                maxNumber="6" id="${figureExpressionSummaryDisplay.figure.zdbID}-terms"/>
                    </c:if>
                    <c:if test="${phenotypeData}">
                        <zfin2:toggledHyperlinkList
                                collection="${figureExpressionSummaryDisplay.phenotypeStatementList}"
                                maxNumber="6" id="${figureExpressionSummaryDisplay.figure.zdbID}-terms"/>
                    </c:if>
                    <c:if test="${expressionGenotypeData}">
                        <zfin2:toggledHyperlinkList
                                collection="${figureExpressionSummaryDisplay.expressionStatementList}"
                                maxNumber="6" id="${figureExpressionSummaryDisplay.figure.zdbID}-terms"/>
                    </c:if>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:if>
