<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinProperties" %>

<%@ attribute name="publications" type="java.util.Collection" required="true" %>
<%--list of PersonMemberPresentation --%>

<c:forEach var="publication" items="${publications}">
    <div class="show_pubs">
        <a href="/${publication.zdbID}">
            ${publication.authors} (${publication.year}) ${publication.title}
            ${publication.journal.abbreviation}${publication.volume}${!empty publication.pages?":":"."}${publication.pages}${!empty publication.pages ? "." : "" }
        </a>
    </div>
</c:forEach>


