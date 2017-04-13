<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult" required="true" %>

<zfin-search:resultTemplate result="${result}">
    <jsp:attribute name="metadata">${result.category}</jsp:attribute>
    <jsp:attribute name="curatorContent">
        <c:set var="pubLinkExtras">&anon1=zdb_id&anon1text=${result.id}</c:set>
        <div class="pub-actions pull-right">
            <a class="btn btn-mini btn-primary btn-pub-actions" href="/action/curation/${result.id}">Curate</a>
            <a class="btn btn-mini btn-default btn-pub-actions" href="/action/publication/${result.id}/track">Track</a>
            <a class="btn btn-mini btn-default btn-pub-actions" href="/action/publication/${result.id}/link">Link</a>
            <a class="btn btn-mini btn-default btn-pub-actions" href="/action/publication/${result.id}/edit">Edit</a>
        </div>
    </jsp:attribute>
    <jsp:body>
    </jsp:body>
</zfin-search:resultTemplate>