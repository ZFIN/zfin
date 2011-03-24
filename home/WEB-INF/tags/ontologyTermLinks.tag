<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="term" type="org.zfin.ontology.Term" required="true" %>


<c:if test="${fn:contains(term.ontology.commonName,'Gene Ontology')}">
 &nbsp;&nbsp;<a href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${term.oboID}">QuickGO</a>
 &nbsp;&nbsp;<a href="http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=${term.oboID}">AmiGO</a>
</c:if>



