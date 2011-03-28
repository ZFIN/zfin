<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ tag import="org.zfin.gwt.lookup.ui.Lookup" %>
<%@ tag import="org.zfin.gwt.lookup.ui.LookupTable" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@attribute name="ontologyName" type="java.lang.String" %>
<%@attribute name="ontology" type="org.zfin.ontology.Ontology" %>
<%@attribute name="id" type="java.lang.String" %>
<%@attribute name="action" type="java.lang.String" required="false" %>
<%@attribute name="wildcard" type="java.lang.Boolean" required="true" description="Allow to enter a query string with a wildcard added." %>

<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>

<script type="text/javascript">
    var LookupProperties${id} = {
        <%= Lookup.JSREF_DIV_NAME%>: "term-${id}",
    <%= Lookup.JSREF_INPUT_NAME%>: "searchTerm",
    <%= Lookup.JSREF_SHOWERROR%>: true,
    <c:if test="${not empty action}">
    <%= Lookup.JSREF_ACTION%>: "${action}",
    </c:if>
    <%= Lookup.JSREF_TYPE%>: "<%= LookupComposite.GDAG_TERM_LOOKUP%>",
    <c:choose>
    <c:when test="${!empty ontology}">
    <%= Lookup.JSREF_ONTOLOGY_NAME%>: "${ontology}",
    </c:when>
    <c:when test="${!empty ontologyName}">
    <%= Lookup.JSREF_ONTOLOGY_NAME%>: "${ontologyName}",
    </c:when>
    </c:choose>
    <%= Lookup.JSREF_WILDCARD%>: ${wildcard},
    <%= LookupTable.JSREF_USE_TERM_TABLE%>: false,
    <%= LookupTable.JSREF_USE_ID_AS_TERM%>: true,
    <%= Lookup.JSREF_LIMIT%>: 25
    }

</script>

<div id="term-${id}"></div>

