<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<table class="searchresults">
    <caption>
        Expression Pattern Search Results<br>
        <small>(${criteria.numFound} genes with expression)
    </caption>
    <tr>
        <th>Gene</th>
        <th>Expression Data</th>
        <th>Stage Range</th>
        <th>Matching Text</th>
    </tr>
    <c:forEach items="${criteria.geneResults}" var="result" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${criteria.geneResults}" groupByBean="gene.zdbID">
            <td><zfin:link entity="${result.gene}"/></td>
            <td>
                <c:choose>
                    <c:when test="${result.figureCount == 1}">
                        <zfin:link entity="${result.singleFigure}" />
                    </c:when>
                    <c:otherwise>
                        <a href="${criteria.getUrl(result.gene)}">
                                ${result.figureCount} Figures
                        </a>
                    </c:otherwise>
                </c:choose>

                from

                <c:choose>
                    <c:when test="${result.publicationCount == 1}">
                        <zfin:link entity="${result.singlePublication}" />
                    </c:when>
                    <c:otherwise>
                        ${result.publicationCount} Publications
                    </c:otherwise>
                </c:choose>
                <c:out value=" "/>
                <zfin2:showCameraIcon hasImage="${result.hasImage}" />
            </td>
            <td>
                <zfin2:stageRange earliestStartStage="${result.startStage}" latestEndStage="${result.endStage}" />
            </td>
            <td>
                <c:out value="${result.matchingText}" escapeXml="false"/>
            </td>
        </zfin:alternating-tr>
    </c:forEach>

</table>

<div style="padding: 10px 0">
    <zfin2:pagination paginationBean="${paginationBean}"/>
</div>