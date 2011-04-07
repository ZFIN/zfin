<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

Back to <a href="/action/dev-tools/home"> Dev-tools</a>
<p></p>

<span class="summaryTitle">List of Ontologies</span>
<table class="searchresults">
    <tr style="background: #ccc">
        <th>Name [subset]</th>
        <th>Version</th>
        <th>Date</th>
        <th>Modified By</th>
        <th>Default Namespace</th>
        <th>Reload from DB</th>
    </tr>
    <c:forEach var="metaData" items="${formBean.metadataList}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${formBean.metadataList}">
            <td>${metaData.name}
                <c:if test="${metaData.subsets  != null}">
                <ol>
                    <c:forEach var="subset" items="${metaData.subsets}">
                            <li title="${subset.internalName}"> ${subset.name}</li>
                    </c:forEach>
                </ol>
            </c:if>
            </td>
            <td>${metaData.oboVersion}</td>
            <td>${metaData.date}</td>
            <td>${metaData.savedBy}</td>
            <td>${metaData.defaultNamespace}</td>
            <td><a href="reload-ontology?ontologyName=${metaData.name}">${metaData.name}</a></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
