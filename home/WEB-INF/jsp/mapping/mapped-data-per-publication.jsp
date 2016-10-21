<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:dataManager zdbID="${publication.zdbID}"/>

<p/>

<div class="titlebar">
    <h1>Mapping Data for <zfin:link entity="${publication}"/>  (${fn:length(mappedEntities)} records)</h1>
</div>

<p/>
<table class="summary rowstripes sortable">
    <caption>Linkage Memberships</caption>
    <tr>
        <th width="100"> Entity Type</th>
        <th width="200"> Entity Symbol</th>
        <th> Location</th>
    </tr>
    <c:forEach var="entity" items="${mappedEntities}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>${entity.entityType}</td>
            <td><zfin:link entity="${entity}"/></td>
            <td><zfin2:displayLocation entity="${entity}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

