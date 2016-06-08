<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>

<c:set var="definitionAttribute" value="<%=ResultService.DEFINITION%>"/>
<c:set var="stageAttribute" value="<%=ResultService.EXISTS_DURING%>"/>
<c:set var="aliasAttribute" value="<%=ResultService.SYNONYMS%>"/>


<table class="table-results searchresults" style="display: none;">
    <th>Term Name</th>
    <th>Synonyms</th>
    <th>Definition</th>
    <th>Exists During</th>
    <th>Related Data</th>
    <th>OBO ID</th>
    <th></th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>
            <td>${result.attributes[aliasAttribute]}</td>
            <td>${result.attributes[definitionAttribute]}</td>
            <td>${result.attributes[stageAttribute]}</td>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
            <td>${result.id}</td>
        </zfin:alternating-tr>
    </c:forEach>
</table>