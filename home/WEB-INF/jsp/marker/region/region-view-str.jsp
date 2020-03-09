<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="mutantsOnMarkerBean" value="${formBean.mutantOnMarkerBeans}"/>
<c:set var="marker" value="${formBean.marker}"/>

<z:dataTable collapse="false"
             hasData="${!empty mutantsOnMarkerBean and (!empty mutantsOnMarkerBean.knockdownReagents)}">
    <c:set var="sequenceTargetingReagentBeans" value="${mutantsOnMarkerBean.knockdownReagents}"/>
    <tr>
        <th>Targeting Reagent</th>
        <th>Created Alleles</th>
        <th>Publications</th>
    </tr>
    <c:forEach items="${sequenceTargetingReagentBeans}" var="bean" varStatus="loop">
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
            <td style="vertical-align: text-top; text-align: left">
                <a href="/action/marker/citation-list/${bean.marker.zdbID}">${fn:length(bean.marker.publications)}</a>
            </td>
        </tr>
    </c:forEach>
</z:dataTable>