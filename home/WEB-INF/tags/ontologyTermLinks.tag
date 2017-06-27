<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="term" type="org.zfin.ontology.Term" required="true" %>

<c:if test="${!empty term && fn:startsWith(term.ontology.commonName,'GO')}">
 &nbsp;&nbsp;<a href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${term.oboID}">QuickGO</a>
 &nbsp;&nbsp;<a href="http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=${term.oboID}">AmiGO</a>
</c:if>
<c:if test="${!empty term && term.ontology.commonName eq 'Human Disease'}">
 &nbsp;&nbsp;(<a href=" http://www.disease-ontology.org/?id=${term.oboID}">${term.oboID}</a>)
</c:if>



