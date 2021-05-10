<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable collapse="true" hasData="${!empty formBean.diseaseModelDisplays}">
    <thead>
        <tr>
            <th>Human Disease</th>
            <th>Fish</th>
            <th>Conditions</th>
            <th>Citations</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${formBean.diseaseModelDisplays}" var="disease" varStatus="loop">
            <tr>
                <td><zfin:link entity="${disease.disease}"/></td>
                <td><zfin:link entity="${disease.experiment.fish}"/></td>
                <td><zfin:link entity="${disease.experiment.experiment}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${fn:length(disease.publications) == 1}">
                            <zfin:link entity="${disease.publications[0]}"/>
                        </c:when>
                        <c:otherwise>
                            <a href="/action/ontology/fish-model-publication-list/${disease.disease.oboID}/${disease.experiment.fish.zdbID}">
                                (${fn:length(disease.publications)})
                            </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</z:dataTable>