<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="diseaseModels" type="java.util.Collection" %>
<%@ attribute name="gene" type="org.zfin.marker.Marker" %>


<zfin2:subsection title="DISEASE ASSOCIATED WITH <i>${gene.abbreviation}</i> VIA EXPERIMENTAL MODELS <a class='popup-link info-popup-link' href='/action/marker/note/disease-model'></a>" showNoData="true"
                  test="${fn:length(diseaseModels) ne null && fn:length(diseaseModels) > 0}">
    <table class="summary groupstripes">
        <thead>
        <tr>
            <th>Human Disease</th>
            <th>Fish</th>
            <th>Conditions</th>
            <th>Citations</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${diseaseModels}" var="disease" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${formBean.diseaseModelDisplays}" groupByBean="disease.termName">
                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${formBean.diseaseModelDisplays}" groupByBean="disease.termName">
                        <zfin:link entity="${disease.disease}"/>
                    </zfin:groupByDisplay>
                </td>
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
            </zfin:alternating-tr>
        </c:forEach>
        </tbody>
    </table>
</zfin2:subsection>