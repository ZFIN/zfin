<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<table class="searchresults groupstripes">
    <caption>
        ${criteria.numFound} Figures
    </caption>
    <tr>
        <th>Publication</th>
        <th>Data</th>
        <th>Fish</th>
        <th>Stage Range</th>
        <th>Anatomy</th>
    </tr>
    <c:forEach items="${criteria.figureResults}" var="result" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${criteria.figureResults}" groupByBean="publication.zdbID">
            <td>
            <zfin:groupByDisplay loopName="loop" groupBeanCollection="${criteria.figureResults}" groupByBean="publication.zdbID">
                <zfin:link entity="${result.publication}"/></td>
            </zfin:groupByDisplay>
            <td>
                <zfin:link entity="${result.figure}"/>
            </td>
            <td><zfin:link entity="${result.fish}"/></td>
            <td></td>
            <td></td>
        </zfin:alternating-tr>
    </c:forEach>

</table>

<div style="clear: both ; width: 80%" class="clearfix">
    <zfin2:pagination paginationBean="${paginationBean}"/>
</div>