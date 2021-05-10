<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>
<c:set var="aliasAttribute" value="${ResultService.SYNONYMS}"/>
<c:set var="sourceAttribute" value="${ResultService.SOURCE}"/>
<c:set var="typeAttribute" value="${ResultService.TYPE}"/>
<c:set var="hostOrganismAttribute" value="${ResultService.HOST_ORGANISM}"/>


<table class="table-results searchresults" style="display: none;">
    <th>Name</th>
    <th>Synonyms</th>
    <th>Host Organism</th>
    <th>Type</th>
    <th>Source</th>

    <th>Related Data</th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>
            <td>${result.attributes[aliasAttribute]}</td>
            <td>${result.attributes[hostOrganismAttribute]}</td>
            <td>${result.attributes[typeAttribute]}</td>
            <td>${result.attributes[sourceAttribute]}</td>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>