<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@attribute name="ontologyName" type="java.lang.String" %>
<%@attribute name="id" type="java.lang.String" %>
<%@attribute name="action" type="java.lang.String" required="false" %>

<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>

<script type="text/javascript">
    var LookupProperties${id} = {
        divName: "term-${id}",
        inputName: "searchTerm-${id}",
        showError: true,
        showButton: false,
        <c:if test="${not empty action}">
        action: "${action}",
        </c:if>
        type: "<%= LookupComposite.GDAG_TERM_LOOKUP%>",
        ontologyName: "${ontologyName}",
        wildcard: false,
        limit: 25
    };

</script>

<span id="term-${id}"></span>

