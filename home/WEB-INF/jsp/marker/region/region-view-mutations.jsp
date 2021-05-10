<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="mutantsOnMarkerBean" value="${formBean.mutantOnMarkerBeans}"/>
<c:set var="marker" value="${formBean.marker}"/>

<z:dataTable collapse="true"
             hasData="${!empty mutantsOnMarkerBean and (!empty mutantsOnMarkerBean.features )}">
    <c:if test="${!empty mutantsOnMarkerBean.features}">

        <div>
            <table class="summary rowstripes alleles">
                <tr>
                    <th width="10%">Allele</th>
                    <th width="13%">Type</th>
                    <th width="15%">Localization</th>
                    <th width="20%">Consequence</th>
                    <th width="10%">Mutagen</th>
                    <th width="50%">Suppliers</th>
                </tr>
                <c:forEach var="feature" items="${mutantsOnMarkerBean.features}" varStatus="loop">
                    <tr class=${loop.index%2==0 ? "even" : "odd"}>
                        <td>
                            <a href="/${feature.zdbID}">${feature.abbreviation}</a>
                        </td>
                        <td>
                                ${feature.type.display}
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${!empty feature.geneLocalizationStatement}">
                                    ${feature.geneLocalizationStatement}
                                </c:when>
                                <c:otherwise>
                                    <span class="no-data-tag">Unknown</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${!empty feature.transcriptConsequenceStatement}">
                                    ${feature.transcriptConsequenceStatement}
                                </c:when>
                                <c:otherwise>
                                    <span class="no-data-tag">Unknown</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:set var="mutagen" value="${feature.featureAssay.mutagen}"/>
                            <c:if test="${mutagen ne zfn:getMutagen('not specified')}">
                                ${feature.featureAssay.mutagen}
                            </c:if>
                        </td>
                        <td>
                            <c:forEach var="supplier" items="${feature.suppliers}">
                                <li style="list-style-type: none;">
                                    <a href="/${supplier.organization.zdbID}"> ${supplier.organization.name}</a>
                                    <c:if test="${!empty supplier.orderURL}">
                                        <a href="${supplier.orderURL}"> (order this)</a>
                                    </c:if>
                                </li>
                            </c:forEach>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </c:if>

    <c:if test="${!empty mutantsOnMarkerBean.knockdownReagents}">
        <div class="strs">
            <b>Sequence Targeting Reagents</b>
            <zfin2:sequenceTargetingReagentsInGene
                    sequenceTargetingReagentBeans="${mutantsOnMarkerBean.knockdownReagents}"/>
        </div>
    </c:if>
</z:dataTable>