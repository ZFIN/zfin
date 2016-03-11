<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="publications" type="java.util.Collection" required="true" %>

<c:forEach var="publication" items="${publications}">
    <div class="show_pubs">
        <a href="/${publication.zdbID}">${publication.citation}</a>
    </div>
</c:forEach>


