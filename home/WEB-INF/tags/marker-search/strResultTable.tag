<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="criteria" type="org.zfin.search.presentation.MarkerSearchCriteria"%>

<table class="searchresults groupstripes">
    <caption></caption>
    <tr>
        <th>Gene</th>
        <th>Reagent</th>
        <th>Matching Text</th>
    </tr>
    <c:forEach var="result" items="${criteria.results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop"  groupBeanCollection="${criteria.results}" groupByBean="targetGene.abbreviation">
            <td>
                <zfin:groupByDisplay loopName="loop" groupBeanCollection="${criteria.results}" groupByBean="targetGene.abbreviation">
                    <zfin:link entity="${result.targetGene}"/>
                </zfin:groupByDisplay>
            </td>
            <td><zfin:link entity="${result.marker}"/></td>
            <td>${result.matchingText}</td>
        </zfin:alternating-tr>
    </c:forEach>

</table>