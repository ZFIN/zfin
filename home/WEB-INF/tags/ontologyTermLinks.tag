<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.ontology.Ontology" %>

<%@ attribute name="term" type="org.zfin.ontology.Term" required="true" %>

<c:if test="${!empty term && (term.ontology == Ontology.GO_CC || term.ontology == Ontology.GO_BP || term.ontology == Ontology.GO_MF)}">
 &nbsp;&nbsp;<zfin2:externalLink href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${term.oboID}">QuickGO</zfin2:externalLink>
 &nbsp;&nbsp;<zfin2:externalLink href="http://amigo.geneontology.org/amigo/term/${term.oboID}">AmiGO</zfin2:externalLink>
</c:if>
<c:if test="${!empty term && term.ontology == Ontology.DISEASE_ONTOLOGY}">
 &nbsp;&nbsp;(<zfin2:externalLink href="http://www.disease-ontology.org/?id=${term.oboID}">${term.oboID}</zfin2:externalLink>)
</c:if>
<c:if test="${!empty term && term.ontology == Ontology.CHEBI}">
 &nbsp;(<zfin2:externalLink href="https://www.ebi.ac.uk/chebi/chebiOntology.do?chebiId=${term.oboID}">EBI</zfin2:externalLink>)
</c:if>



