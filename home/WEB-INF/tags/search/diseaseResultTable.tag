<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>
<c:set var="aliasAttribute" value="<%=ResultService.SYNONYMS%>"/>
<c:set var="definitionAttribute" value="<%=ResultService.DEFINITION%>"/>

<table class="table-results searchresults" style="display: none;">
    <th>DO ID</th>
    <th>Name</th>
    <th>Definition</th>
    <th>Synonyms</th>
    <th>Related Data</th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.id}</td>
            <td>${result.link}</td>
            <td style="word-wrap: break-word">${result.attributes[definitionAttribute]}</td>
            <td>${result.attributes[aliasAttribute]}</td>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>