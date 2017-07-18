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
                <a href="${criteria.getUrl(result.gene)}">
                        ${result.figureCount} Figures
                </a>

                from ${result.publicationCount} Publications
            </td>
            <td>
                <zfin2:stageRange earliestStartStage="${result.startStage}" latestEndStage="${result.endStage}" />
            </td>
            <td></td>
        </zfin:alternating-tr>
    </c:forEach>

</table>

<div style="padding: 10px 0">
    <zfin2:pagination paginationBean="${paginationBean}"/>
</div>