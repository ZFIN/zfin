<%@ tag import="org.zfin.ontology.presentation.OntologyBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.ontology.presentation.OntologyBean" required="true" %>
<%@ attribute name="action" type="java.lang.String" required="true" %>

<h2>List of Relationship Types for [${formBean.ontologyName}] ontology</h2>

Total of: ${fn:length(zfn:getDistinctRelationshipTypes(formBean.ontology))}

<table width="90%">
    <tr class="search-result-table-header left-top-aligned">
        <td width="50" class="sectionTitle">ID</td>
        <td width="300" class="sectionTitle">Relationship Name</td>
    </tr>

    <c:forEach var="relation" items="${zfn:getDistinctRelationshipTypes(formBean.ontology)}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                        ${loop.index+1}
                </td>
                <td>
                        ${relation}
                </td>
            </zfin:alternating-tr>
    </c:forEach>
</table>