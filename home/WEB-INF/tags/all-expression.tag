<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="expressionSummaryDisplay" type="java.util.Collection" required="false" %>
<%@ attribute name="suppressMoDetails" type="java.lang.Boolean" required="false" %>
<%@ attribute name="showNumberOfRecords" type="java.lang.Integer" required="false" %>
<%@ attribute name="queryKeyValuePair" type="java.lang.String" required="false" %>

<c:if test="${empty showNumberOfRecords}">
    <c:set var="showNumberOfRecords" value="${99999999}"/>
</c:if>
<table class="summary rowstripes">
    <thead>
    <tr>
        <th nowrap="nowrap">
            Expressed Gene
        </th>
        <th>
            Anatomy
        </th>
        <th width="35%">
            Figures
        </th>
    </tr>
    </thead>
    <c:forEach var="expressionSummary" items="${expressionSummaryDisplay}" varStatus="loop"
               end="${showNumberOfRecords-1}">
        <zfin:alternating-tr loopName="loop">
            <td>
                <zfin:groupByDisplay loopName="loop"
                                     groupBeanCollection="${expressionSummaryDisplay}"
                                     groupByBean="expressedGene.gene">
                    <zfin:link entity="${expressionSummary.expressedGene.gene}"/>
                </zfin:groupByDisplay>

            </td>
            <td>
                <zfin2:toggledHyperlinkList
                        collection="${expressionSummary.expressedGene.expressionStatements}"
                        maxNumber="6" id="${expressionSummary.expressedGene.gene.zdbID}-terms"/>
            </td>
            <td>
                <zfin2:figuresFromPublication figureData="${expressionSummary.figureData}" queryKeyValuePair="${queryKeyValuePair}&geneID=${expressionSummary.expressedGene.gene.zdbID}"/>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
