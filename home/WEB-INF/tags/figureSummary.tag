<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figureSummaryDisplayList" type="java.util.List" required="true"
              description="List of FigureSummaryDisplay objects" %>

<%@ attribute name="expressionData" type="java.lang.Boolean" required="false" %>

<c:if test="${!empty figureSummaryDisplayList}">

    <table class="summary rowstripes">
        <tr>
            <th align="left" width="20%">Publication</th>
            <th align="left" width="5%">Data</th>
            <th align="left" width="5%"> &nbsp; </th>
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
        <c:forEach var="figureData" items="${figureSummaryDisplayList}" varStatus="status">
            <zfin:alternating-tr loopName="status"
                                 groupBeanCollection="${figureSummaryDisplayList}"
                                 groupByBean="publication">
                <td>
                    <zfin:groupByDisplay loopName="status"
                                         groupBeanCollection="${figureSummaryDisplayList}"
                                         groupByBean="publication">
                        <zfin:link entity="${figureData.publication}"/>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    <zfin:link entity="${figureData.figure}"/>
                </td>
                <td>
                    <c:if test="${figureData.thumbnail != null}">
                        <zfin:link entity="${figureData.figure}">
                            <img border="1" src="/imageLoadUp/${figureData.thumbnail}" height="50"
                                 title="${figureData.imgCount} image<c:if test="${figureData.imgCount > 1}">s</c:if>"
                                    />
                            <c:if test="${figureData.imgCount > 1}"><img border="0"
                                                                         src="/images/multibars.gif"/></c:if>
                        </zfin:link>
                    </c:if>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${expressionData}">
                            <zfin2:toggledHyperlinkList collection="${figureData.expressionStatementList}"
                                                        maxNumber="6" id="${figureData.figure.zdbID}-terms"/>
                        </c:when>
                        <c:otherwise>
                            <zfin2:toggledHyperlinkList collection="${figureData.phenotypeStatementList}"
                                                        maxNumber="6" id="${figureData.figure.zdbID}-terms" commaDelimited="false"/>
                        </c:otherwise>
                    </c:choose>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:if>
