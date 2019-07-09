<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult" required="true" %>

<zfin-search:resultTemplate result="${result}">
    <jsp:attribute name="metadata">${result.category}</jsp:attribute>
    <jsp:attribute name="curatorContent">
        <c:set var="pubLinkExtras">&anon1=zdb_id&anon1text=${result.id}</c:set>
        <div class="pub-actions">
            <c:if test="${result.curatable}">
                <a class="btn btn-primary" href="/action/curation/${result.id}" role="button">Curate</a>
            </c:if>
            <a class="btn btn-default" href="/action/publication/${result.id}/track" role="button">Track</a>
            <a class="btn btn-default" href="/action/publication/${result.id}/link" role="button">Link</a>
            <a class="btn btn-default" href="/action/publication/${result.id}/edit" role="button">Edit</a>
        </div>
    </jsp:attribute>
    <jsp:body>
    </jsp:body>
</zfin-search:resultTemplate>