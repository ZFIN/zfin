<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult" required="true" %>

<zfin-search:resultTemplate result="${result}">
    <jsp:attribute name="metadata">${result.category}</jsp:attribute>
    <jsp:body>
        <c:if test="${!empty result.featureGenes}">
            <table class="fish-result-table">
                <tr>
                    <th>Affected Gene</th>
                    <th>Line / Reagent</th>
                    <th>Mutation Type</th>
                    <th>Construct</th>
                </tr>
                <c:forEach var="featureGene" items="${result.featureGenes}">
                    <tr>
                        <td title="Affected Gene">
                            <zfin:link entity="${featureGene.gene}"/>
                        </td>
                        <td title="Line / Reagent">
                            <c:if test="${!empty featureGene.feature}">
                                <zfin:link entity="${featureGene.feature}"/>
                            </c:if>
                            <c:if test="${!empty featureGene.sequenceTargetingReagent}">
                                <zfin:link entity="${featureGene.sequenceTargetingReagent}"/>
                            </c:if>
                        </td>
                        <td title="Mutation Type">
                            <c:if test="${!empty featureGene.feature}">
                                ${featureGene.feature.type.display}
                            </c:if>
                        </td>
                        <td title="Construct">
                            <zfin:link entity="${featureGene.construct}"/>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
    </jsp:body>
</zfin-search:resultTemplate>
