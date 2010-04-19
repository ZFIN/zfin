<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<%-- display aliases --%>
<c:if test="${formBean.actionType.name eq 'SHOW_ALIASES'}" >
    <zfin-ontology:showAllAliases formBean="${formBean}"/>
</c:if>

<%-- display obsolete terms --%>
<c:if test="${formBean.actionType.name eq 'SHOW_OBSOLETE_TERMS'}" >
    <zfin-ontology:showTerms formBean="${formBean}" action="${formBean.actionType.name}"/>
</c:if>

<%-- display all terms --%>
<c:if test="${formBean.actionType.name eq 'SHOW_ALL_TERMS'}" >
    <zfin-ontology:showTerms formBean="${formBean}" action="${formBean.actionType.name}"/>
</c:if>


