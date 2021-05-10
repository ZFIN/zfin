<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
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
        ${LookupStrings.JSREF_DIV_NAME}: "term_${id}",
        ${LookupStrings.JSREF_INPUT_NAME}: "searchTerm",
        ${LookupStrings.JSREF_SHOWERROR}: true,
        <c:if test="${not empty action}">
        ${LookupStrings.JSREF_ACTION}: "${action}",
        </c:if>
        ${LookupStrings.JSREF_TYPE}: "${LookupStrings.GDAG_TERM_LOOKUP}",
        <c:if test="${!empty ontologyName}">
        ${LookupStrings.JSREF_ONTOLOGY_NAME}: "${ontologyName}",
        </c:if>
        <c:if test="${!empty ontologyName && ontologyName eq 'zebrafish_anatomy'}">
        ${LookupStrings.JSREF_ANATOMY_TERMS_ONLY}: true,
        </c:if>
        ${LookupStrings.JSREF_WILDCARD}: ${wildcard},
        ${LookupStrings.JSREF_USE_TERM_TABLE}: false,
        ${LookupStrings.JSREF_USE_ID_AS_TERM}: ${useIdAsTerm},
        ${LookupStrings.JSREF_TERMS_WITH_DATA_ONLY}: ${termsWithDataOnly},
        ${LookupStrings.JSREF_LIMIT}: 25
    }
</script>

<div id="term_${id}"></div>

