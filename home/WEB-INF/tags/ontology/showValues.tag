<%@ tag import="org.zfin.ontology.presentation.OntologyBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.ontology.presentation.OntologyBean" required="true" %>
<%@ attribute name="action" type="java.lang.String" required="true" %>

<h2>List of All Values for [${formBean.ontologyName}] ontology</h2>

Total of: ${fn:length(formBean.terms)}

<table width="90%">
    <tr class="search-result-table-header left-top-aligned">
        <td width="50" class="sectionTitle">ID</td>
        <td width="300" class="sectionTitle">Term Name</td>
        <td class="sectionTitle">Keys</td>
    </tr>

    <c:forEach var="value" items="${formBean.valueMap}" varStatus="loop">
        <tr class="search-result-table-entries left-top-aligned">
            <td>
                    ${loop.index+1}
            </td>
            <td class="listContentBold">
                <zfin:link entity="${value.key}"/>
            </td>
            <td>
                <c:forEach var="key" items="${value.value}" varStatus="loop">
                    ${key}  || 
                </c:forEach>
            </td>
        </tr>
    </c:forEach>
</table>

