<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.ontology.presentation.OntologyBean" required="true" %>

<h2>List of Aliases for [${formBean.ontologyName}] ontology</h2>

Total of: ${fn:length(formBean.aliasTermMap)}

<table width="90%">
    <tr class="search-result-table-header left-top-aligned">
        <td width="50" class="sectionTitle">ID</td>
        <td width="300" class="sectionTitle">Alias Name</td>
        <td class="sectionTitle">Term Name(s)</td>
        <td class="sectionTitle">Alias Type</td>
    </tr>

    <c:forEach var="value" items="${formBean.aliasTermMap}" varStatus="loop">
        <tr class="search-result-table-entries left-top-aligned">
            <td>
                ${loop.index}
            </td>
            <td class="listContentBold">
                    <c:out value="${value.key}" escapeXml="true" />
            </td>
            <td>
                <c:forEach var="value" items="${value.value}" >
                    <zfin:link entity="${value.term}"/>
                    <br/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="termTwo" items="${value.value}" >
                    ${termTwo.group} <br/>
                </c:forEach>
            </td>
        </tr>
    </c:forEach>
</table> 