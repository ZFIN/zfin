<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<table class="searchresults groupstripes">
    <caption>
        Expression Pattern Search Results for <zfin:link entity="${criteria.gene}"/><br>
        <small>
            (<zfin:choice choicePattern="0#figures|1#figure|2#figures" includeNumber="true" integerEntity="${criteria.numFound}"/>
            with expression from
            <zfin:choice choicePattern="0#publications|1#publication|2#publications" includeNumber="true" integerEntity="${criteria.pubCount}"/>)
        </small>
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

<div style="padding: 10px 0">
    <zfin2:pagination paginationBean="${paginationBean}"/>
</div>