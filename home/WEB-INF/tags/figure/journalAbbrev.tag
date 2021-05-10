<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="publication" type="org.zfin.publication.Publication"%>

<c:if test="${!empty publication.doi && !empty publication.journal}">
     <a href="http://dx.doi.org/${publication.doi}"> Full text @ ${publication.journal.abbreviation}</a>
</c:if>