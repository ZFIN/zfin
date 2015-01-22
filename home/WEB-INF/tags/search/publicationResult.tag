<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult" required="true" %>

<zfin-search:resultTemplate result="${result}">
    <jsp:attribute name="metadata">${result.category}</jsp:attribute>
    <jsp:attribute name="curatorContent">
        <c:set var="pubLinkExtras">&anon1=zdb_id&anon1text=${result.id}</c:set>
        <div class="pub-actions pull-right">
            <a class="btn btn-mini btn-primary btn-pub-actions" href="/cgi-bin/webdriver?MIval=aa-curation.apg&OID=${result.id}">Curate</a>
            <a class="btn btn-mini btn-default btn-pub-actions" href="/cgi-bin/webdriver?MIval=aa-pubcuration.apg&OID=${result.id}">Track</a>
            <a class="btn btn-mini btn-default btn-pub-actions" href="/cgi-bin/webdriver?MIval=aa-link_authors.apg&OID=${result.id}${pubLinkExtras}">Link</a>
            <a class="btn btn-mini btn-default btn-pub-actions" href="/cgi-bin/webdriver?MIval=aa-edit_pub.apg&OID=${result.id}${pubLinkExtras}">Edit</a>
        </div>
    </jsp:attribute>
    <jsp:body>
    </jsp:body>
</zfin-search:resultTemplate>