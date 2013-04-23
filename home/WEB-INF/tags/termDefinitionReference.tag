<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="term" type="org.zfin.ontology.Term" required="true" %>
<%@ attribute name="reference" type="org.zfin.ontology.TermDefinitionReference" required="true" %>

<c:choose>
    <c:when test="${jspFunctions.isZfinData(reference.reference)}">
        <zfin:link entity="${reference.reference}"/>
    </c:when>
    <c:otherwise>
        <a href="${term.getReferenceLink(reference)}">${term.getDefinitionDisplay(reference)}</a>
    </c:otherwise>
</c:choose>
