<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>
<c:set var="term" value="${formBean.term}"/>

<div class="__react-root"
     id="TermRelationshipListView"
     data-term-id="${term.zdbID}"
></div>
