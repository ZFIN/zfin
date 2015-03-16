<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:dataManager zdbID="${linkage.zdbID}" rtype="linkage"/>

<p/>
<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td class="titlebar" style="">
                <span style="font-size: x-large; margin-left: 0.5em; font-weight: bold;">
                        Mapping Data for <zfin:link entity="${publication}"/>  (${fn:length(mappedEntities)} records)
            </span>
        </td>
    </tr>
</table>

<p/>
<table class="summary rowstripes sortable">
    <caption>Linkage Memberships</caption>
    <tr>
        <th width="100"> Entity Type</th>
        <th> Entity Symbol</th>
    </tr>
    <c:forEach var="entity" items="${mappedEntities}">
        <tr>
            <td>${entity.entityType}</td>
            <td><zfin:link entity="${entity}"/></td>
        </tr>
    </c:forEach>
</table>

