<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable collapse="true" hasData="${!empty formBean.diseaseDisplays}">
    <thead>
        <tr>
            <th width="25%">Disease Ontology Term</th>
            <th width="20%">Multi-Species Data</th>
            <th width="25%">OMIM Term</th>
            <th width="20%">OMIM Phenotype ID</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="row" items="${formBean.diseaseDisplays}" varStatus="loop">
            <tr>
                <td>
                    <c:if test="${!empty row.diseaseTerm}">
                        <zfin:link entity="${row.diseaseTerm}" longVersion="true"/>
                    </c:if>
                </td>

                <td>
                    <c:if test="${!empty row.diseaseTerm}">
                        <a href="http://www.alliancegenome.org/disease/${row.diseaseTerm.oboID}">Alliance</a>
                    </c:if>
                </td>

                <td>${row.omimPhenotype.name}</td>

                <td>
                    <c:if test="${!empty row.omimPhenotype.omimNum}">
                        <a href="http://omim.org/entry/${row.omimPhenotype.omimNum}">${row.omimPhenotype.omimNum}</a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</z:dataTable>