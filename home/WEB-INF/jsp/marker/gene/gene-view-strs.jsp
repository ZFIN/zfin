<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-prototype:dataTable collapse="true" hasData="${!empty formBean.mutantOnMarkerBeans.knockdownReagents}">
    <thead>
        <tr>
            <th>Targeting Reagent</th>
            <th>Created Alleles</th>
            <th>Publications</th>
        </tr>
    </thead>

    <c:forEach items="${formBean.mutantOnMarkerBeans.knockdownReagents}" var="bean" varStatus="loop">
        <tr>
            <td><zfin:link entity="${bean.marker}"/></td>
            <td>
                <c:choose>
                    <c:when test="${bean.marker.type == 'MRPHLNO'}">
                        <i class="no-data-tag">N/A</i>
                    </c:when>
                    <c:otherwise>
                        <ul class="comma-separated">
                            <c:forEach items="${bean.genomicFeatures}" var="feature">
                                <li><zfin:link entity="${feature}"/></li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <a href="/action/marker/citation-list/${bean.marker.zdbID}">${fn:length(bean.marker.publications)}</a>
            </td>
        </tr>
    </c:forEach>
</zfin-prototype:dataTable>