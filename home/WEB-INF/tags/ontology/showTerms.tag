<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.ontology.presentation.OntologyBean" required="true" %>
<%@ attribute name="action" type="java.lang.String" required="true" %>

<h2>List of
    <c:if test="${action eq 'SHOW_OBSOLETE_TERMS'}">
        Obsolete
    </c:if>
    <c:if test="${action eq 'SHOW_ALL_TERMS'}">
        All
    </c:if>
    Terms for [${formBean.ontologyName}] ontology</h2>

Total of: ${fn:length(formBean.termMap)}

<table width="90%">
    <tr class="search-result-table-header left-top-aligned">
        <td width="50" class="sectionTitle">ID</td>
        <td width="300" class="sectionTitle">Term Name</td>
        <td class="sectionTitle">Obo ID</td>
        <td class="sectionTitle">Term Name</td>
        <td class="sectionTitle">ID</td>
    </tr>

    <c:forEach var="dataMap" items="${formBean.termMap}" varStatus="loop">
        <tr class="search-result-table-entries left-top-aligned">
            <td>
                    ${loop.index+1}
            </td>
            <td class="listContentBold">
                <zfin:link entity="${dataMap.value}"/>
            </td>
            <td>
                    ${dataMap.value.oboID}
            </td>
            <td>
                    ${dataMap.value.ID}
            </td>
        </tr>
    </c:forEach>
</table>