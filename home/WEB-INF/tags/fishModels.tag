<%@ attribute name="fishModels" type="java.util.Collection" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<zfin2:subsection title="ZEBRAFISH MODELS"
                  test="${fn:length(fishModels) ne null && fn:length(fishModels) > 0}">
    <table class="summary groupstripes">

        <tr>
            <th>Fish</th>
            <th>
                Conditions
            </th>
            <th>
                Citations
            </th>
        </tr>
        <c:forEach var="fishModel" items="${fishModels}" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${fishModels}"
                                 groupByBean="fishModel.fish.name">

                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${fishModels}"
                                         groupByBean="fishModel.fish.name">

                        <zfin:link entity="${fishModel.fishModel.fish}"/>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    ${fishModel.fishModel.experiment.name}
                </td>
                <td>
                    <c:choose>
                        <c:when test="${fishModel.publications.size() == 1}">
                            (<a href="/${fishModel.publication.zdbID}">1</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="/action/ontology/${fishModel.publication.zdbID}">${fishModel.publications.size()}</a>)
                        </c:otherwise>
                    </c:choose>
                </td>

            </zfin:alternating-tr>
        </c:forEach>
    </table>
</zfin2:subsection>