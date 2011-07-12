<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="publication" type="org.zfin.publication.Publication"%>
<%@ attribute name="webdriverPath" type="java.lang.String" %>

<a href="/${webdriverPath}?MIval=aa-pubview2.apg&OID=${publication.zdbID}">
    ${publication.authors} (${publication.year}) ${publication.title}.
    ${publication.journal.abbreviation}
        ${publication.volume}
        ${publication.pages}
</a>
