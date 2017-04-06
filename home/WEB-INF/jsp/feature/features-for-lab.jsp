<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<br/>
<table class="summary rowstripes">
    <tr>
        <th width="5%">Allele</th>
        <th width="7%">Type</th>
        <th width="10%">Affected Genomic Region</th>
        <th width="10%">Construct</th>
    </tr>
    <c:forEach var="feature" items="${features}" varStatus="loop">
        <tr class=${loop.index%2==0 ? "even" : "odd"}>
            <td>
                <zfin:link entity="${feature}"/>
            </td>
            <td>
                    ${feature.type.display}
            </td>
            <td>
                <c:forEach var="gene" items="${feature.featureMarkerRelations}">
                    <li style="list-style-type: none;">
                        <c:if test="${gene.featureMarkerRelationshipType.affectedMarkerFlag eq 'true'}">
                            <a href="/${gene.marker.zdbID}"> <i>${gene.marker.abbreviation}</i></a>
                        </c:if>
                    </li>
                </c:forEach>
            </td>
            <td>
                <c:forEach var="construct" items="${feature.getConstructs()}">
                    <li style="list-style-type: none;">
                        <a href="/${construct.marker.zdbID}"> ${construct.marker.abbreviation}</a>
                    </li>
                </c:forEach>
            </td>
        </tr>
    </c:forEach>
</table>
<a href="/search?q=&amp;fq=category%3A%22Mutation+%2F+Tg%22&amp;fq=xref%3A%22${labID}%22">Browse all ${numOfFeatures} features </a>
