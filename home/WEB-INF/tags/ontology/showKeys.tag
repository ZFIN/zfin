<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.ontology.presentation.OntologyBean" required="true" %>
<%@ attribute name="action" type="java.lang.String" required="true" %>

<h2>List of
    <c:if test="${action eq 'SHOW_ALL_TERMS'}">
        All
    </c:if>
    Terms for [${formBean.ontologyName}] ontology</h2>

Total of: ${fn:length(formBean.keys)}

<table width="90%">
    <tr class="search-result-table-header left-top-aligned">
        <td width="50" class="sectionTitle">ID</td>
        <td width="300" class="sectionTitle">Term Key</td>
        <td colspan="1" class="sectionTitle">Number Terms</td>
        <td colspan="3" class="sectionTitle">Term Value</td>
    </tr>

    <c:forEach var="value" items="${formBean.keys}" varStatus="loop">
        <tr class="search-result-table-entries left-top-aligned">
            <td>
                    ${loop.index+1}
            </td>
            <td>
                ${value.key}
            </td>
            <td>
                    ${fn:length(value.value)}
            </td>
            <td colspan="3">
               <zfin:link entity="${value.value}"/>
            </td>
        </tr>
    </c:forEach>
</table>


