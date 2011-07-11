<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="publication" type="org.zfin.publication.Publication"%>
<%@ attribute name="webdriverPath" type="java.lang.String" %>

<%--//        <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-pubview2.apg&OID=$pubrd_pub_zdb_id">--%>
    <%--// $pubrd_pub_authors ($pubrd_pub_pyear) $pubrd_title. $pubrd_jrnl_abbrev
    <%--$pubrd_pub_volume$pubrd_pub_pages&ndash;%&gt;--%>
    <%--// </A>--%>
<a href="/${webdriverPath}?MIval=aa-pubview2.apg&OID=${publication.zdbID}">
    ${publication.authors} (${publication.year}) ${publication.title}.
    ${publication.journal.abbreviation}
        ${publication.volume}
        ${publication.pages}
</a>
