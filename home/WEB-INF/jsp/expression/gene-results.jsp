<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<table class="searchresults">
    <tr>
        <th>Gene</th>
        <th>Expression Data</th>
        <th>Stage Range</th>
        <th>Matching Text</th>
    </tr>
    <c:forEach items="${results}" var="result" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="gene.zdbID">
            <td><zfin:link entity="${result.gene}"/></td>
            <td>
                ${result.figureCount} Figures from ${result.publicationCount} Publications
            </td>
            <td></td>
            <td></td>
        </zfin:alternating-tr>
    </c:forEach>

</table>