<%@ tag import="org.zfin.gwt.lookup.ui.Lookup" %>
<%@ tag import="org.zfin.gwt.lookup.ui.LookupTable" %>
<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@attribute name="ontologyName" type="java.lang.String" %>
<%@attribute name="id" type="java.lang.String" %>
<%@attribute name="action" type="java.lang.String" required="false" %>
<%@attribute name="wildcard" type="java.lang.Boolean" required="true" description="Allow to enter a query string with a wildcard added." %>
<%@attribute name="useIdAsTerm" type="java.lang.Boolean" required="true" %>
<%@attribute name="termsWithDataOnly" type="java.lang.Boolean" required="false" %>

<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>

<script type="text/javascript">
    var LookupProperties${id} = {
        <%= Lookup.JSREF_DIV_NAME%>: "term_${id}",
    <%= Lookup.JSREF_INPUT_NAME%>: "searchTerm",
    <%= Lookup.JSREF_SHOWERROR%>: true,
    <c:if test="${not empty action}">
    <%= Lookup.JSREF_ACTION%>: "${action}",
    </c:if>
    <%= Lookup.JSREF_TYPE%>: "<%= LookupComposite.GDAG_TERM_LOOKUP%>",
    <c:choose>
    <c:when test="${!empty ontologyName}">
    <%= Lookup.JSREF_ONTOLOGY_NAME%>: "${ontologyName}",
    </c:when>
    </c:choose>
    <c:if test="${!empty ontologyName && ontologyName eq 'zebrafish_anatomy'}">
    <%= Lookup.JSREF_ANATOMY_TERMS_ONLY%>: true,
    </c:if>
    <%= Lookup.JSREF_WILDCARD%>: ${wildcard},
    <%= LookupTable.JSREF_USE_TERM_TABLE%>: false,
    <%= LookupTable.JSREF_USE_ID_AS_TERM%>: ${useIdAsTerm},
    <%= Lookup.JSREF_TERMS_WITH_DATA_ONLY%>: ${termsWithDataOnly},
    <%= Lookup.JSREF_LIMIT%>: 25
    }

</script>

<div id="term_${id}"></div>

