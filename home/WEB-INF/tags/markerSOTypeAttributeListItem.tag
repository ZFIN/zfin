<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="soTerm" required="true" type="org.zfin.ontology.GenericTerm" %>

<c:set var="soUrl">http://www.sequenceontology.org/browser/current_svn/term/${soTerm.oboID}</c:set>

<z:attributeListItem label="Type">
    <zfin2:externalLink href="${soUrl}">${soTerm.termName}</zfin2:externalLink>
</z:attributeListItem>
