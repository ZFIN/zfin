<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:dataManager zdbID="${linkage.zdbID}"
                   rtype="linkage"/>

<p/>
<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td class="titlebar" style="">
                <span style="font-size: x-large; margin-left: 0.5em; font-weight: bold;">
                        Linkage Detail
            </span>
        </td>
    </tr>
</table>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Chromosome:</span></th>
        <td><span class="name-value">${linkage.chromosome}</span></td>
    </tr>
    <tr>
        <th><span class="name-label">Comment:</span></th>
        <td><span class="name-value">${linkage.comments}</span></td>
    </tr>
    <tr>
        <th><span class="name-label">Submitter:</span></th>
        <td><span class="name-value"><zfin:link entity="${linkage.person}"/></span></td>
    </tr>
    <tr>
        <th><span class="name-label">Publication:</span></th>
        <td><span class="name-value"><zfin:link entity="${linkage.publication}"/></span></td>
    </tr>
    <tr>
        <th><span class="name-label">Person Reference:</span></th>
        <td><span class="name-value"></span></td>
    </tr>
</table>

<p/>
<p/>
<c:if test="${linkage.linkageMemberSet.size() > 0}">
    <table class="summary rowstripes sortable">
        <caption>Linkage Memberships</caption>
        <tr>
            <th> ID</th>
            <th> Entity One</th>
            <th> Entity Two</th>
            <th> Distance</th>
            <th> Metric</th>
            <th> LOD</th>
        </tr>
        <c:forEach var="member" items="${linkage.linkageMemberSet}">
            <tr>
                <td>${member.id} </td>
                <td width="10%">
                    <zfin:link entity="${member.entityOne}"/>
                </td>
                <td width="10%">
                    <zfin:link entity="${member.entityTwo}"/>
                </td>
                <td>${member.distance} </td>
                <td>${member.metric} </td>
                <td>${member.lod} </td>
            </tr>
        </c:forEach>
    </table>
</c:if>

