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

    <c:forEach var="dataMap" items="${formBean.aliasTermMap}" varStatus="loop">
        <tr class="search-result-table-entries left-top-aligned">
            <td>
                ${loop.index}
            </td>
            <td class="listContentBold">
                    <c:out value="${dataMap.key}" escapeXml="true" />
            </td>
            <td>
                <c:forEach var="term" items="${dataMap.value}" >
                    <zfin:link entity="${term.term}"/>
                    <br/>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="termTwo" items="${dataMap.value}" >
                    ${termTwo.group} <br/>
                </c:forEach>
            </td>
        </tr>
    </c:forEach>
</table> 