<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-prototype:dataTable collapse="true" hasData="${!empty formBean.mutantOnMarkerBeans.features}">
    <thead>
        <tr>
            <th width="10%">Allele</th>
            <th width="13%">Type</th>
            <th width="15%">Localization</th>
            <th width="20%">Consequence</th>
            <th width="10%">Mutagen</th>
            <th width="50%">Suppliers</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="feature" items="${formBean.mutantOnMarkerBeans.features}" varStatus="loop">
            <tr>
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
                        ${feature.featureAssay.mutagen.toString()}
                    </c:if>
                </td>
                <td>
                    <ul class="list-unstyled">
                        <c:forEach var="supplier" items="${feature.suppliers}">
                            <li><a href="/${supplier.organization.zdbID}"> ${supplier.organization.name}</a>
                                <c:if test="${!empty supplier.orderURL}">
                                    <a href="${supplier.orderURL}"> (order this)</a>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ul>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</zfin-prototype:dataTable>