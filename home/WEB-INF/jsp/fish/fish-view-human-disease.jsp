<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable collapse="true"
             hasData="${diseases != null && fn:length(diseases) > 0 }">
    <thead>
        <tr>
            <th>Human Disease</th>
            <th>Conditions</th>
            <th>Citations</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${diseases}" var="disease" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${diseases}"
                                 groupByBean="disease.termName">
                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${diseases}"
                                         groupByBean="disease.termName">
                        <zfin:link entity="${disease.disease}"/>
                    </zfin:groupByDisplay>
                </td>
                <td><zfin:link entity="${disease.experiment.experiment}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${fn:length(disease.publications) == 1}">
                            <zfin:link entity="${disease.publications[0]}"/>
                            <%--<a href="${disease.publications[0].zdbID}">(1)</a>--%>
                        </c:when>
                        <c:otherwise>
                            <a href="/action/ontology/fish-model-publication-list/${disease.disease.oboID}/${disease.experiment.zdbID}">
                                (${fn:length(disease.publications)})
                            </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </tbody>
</z:dataTable>