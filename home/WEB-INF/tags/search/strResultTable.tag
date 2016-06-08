<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>
<c:set var="aliasAttribute" value="<%=ResultService.SYNONYMS%>"/>
<c:set var="seqAttribute" value="<%=ResultService.SEQUENCE%>"/>
<c:set var="tgtGeneAttribute" value="<%=ResultService.TARGETED_GENES%>"/>


<table class="table-results searchresults" style="display: none;">
    <th>Name</th>
    <th>Synonyms</th>
    <th>Type</th>
    <th>Targeted Genes</th>
    <th>Targeted Sequence</th>
    <th>Related Data</th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>
            <td>${result.attributes[aliasAttribute]}</td>
            <td>${result.type}</td>
            <td>${result.attributes[tgtGeneAttribute]}</td>
            <td>${result.attributes[seqAttribute]}</td>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>