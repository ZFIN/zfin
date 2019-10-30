<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequenceTargetingReagentBeans" required="true" rtexprvalue="true" type="java.util.List" %>

<table class="summary rowstripes">
    <thead>
        <tr>
            <th>Targeting Reagent</th>
            <th>Created Alleles</th>
            <th>Publications</th>
        </tr>
    </thead>
    <c:forEach items="${sequenceTargetingReagentBeans}" var="bean" varStatus="loop">
        <tr class=${loop.index % 2 == 0 ? "even" : "odd"}>
            <td><zfin:link entity="${bean.marker}" /></td>
            <td>
                <c:choose>
                    <c:when test="${bean.marker.type == 'MRPHLNO'}">
                        <i class="no-data-tag">N/A</i>
                    </c:when>
                    <c:otherwise>
                        <ul class="comma-separated">
                            <c:forEach items="${bean.genomicFeatures}" var="feature">
                                <li><zfin:link entity="${feature}" /></li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                (<a href="/action/marker/citation-list/${bean.marker.zdbID}">${fn:length(bean.marker.publications)}</a>)
            </td>
        </tr>
    </c:forEach>
</table>