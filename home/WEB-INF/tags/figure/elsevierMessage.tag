<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="publication" type="org.zfin.publication.Publication"%>
<%@attribute name="showElsevierMessage" type="java.lang.Boolean" rtexprvalue="true" required="true" %>


<c:if test="${showElsevierMessage}">
    <br><br>Reprinted from ${publication.journal.name}, ${publication.volume}, ${publication.authors}, ${publication.title}, ${publication.pages}, Copyright
    (<fmt:formatDate value="${publication.publicationDate.time}" type="Date" pattern="yyyy" />) with permission from Elsevier.
</c:if>
