<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult" required="true" %>

<zfin-search:resultTemplate result="${result}">
    <jsp:attribute name="metadata">${result.category}</jsp:attribute>
    <jsp:body>
        <c:if test="${!empty result.resultTableMap}">
            <table class="fish-result-table">
                <tr>
                    <th>Affected Gene</th>
                    <th>Line / Reagent</th>
                    <th>Mutation Type</th>
                    <th>Construct</th>
                </tr>
                <c:forEach var="resultRowMap" items="${result.resultTableMap}">
                    <tr>
                        <td title="Affected Gene">
                            <span class="genedom">${resultRowMap["gene"]}</span>
                        </td>
                        <td title="Line / Reagent">
                                ${resultRowMap["affector"]}
                        </td>
                        <td title="Mutation Type">
                                ${resultRowMap["affectorType"]}
                        </td>
                        <td title="Construct">
                                ${resultRowMap["construct"]}
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
        <%-- todo: remove this later! --%>
        <c:if test="${empty result.resultTableMap}">
            <span style="font-size: small; font-style: italic; color: #ccc">
                Component table not yet implemented for non-warehouse fish.
            </span>
        </c:if>
    </jsp:body>
</zfin-search:resultTemplate>
