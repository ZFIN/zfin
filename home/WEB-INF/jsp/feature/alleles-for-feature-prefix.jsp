<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<authz:authorize access="hasRole('root')">
    <a href="/zf_info/zfbook/lab_desig.html">Previous Static Lab Line Designations</a>
    <c:if test="${formBean.hasNonCurrentLabs}">
        Labs that have features using this designation but have an alternate designation:
        <c:forEach var="lab" items="${formBean.nonCurrentLabs}" varStatus="status">
            <%--<c:if test="${lab.lab.active}">--%>
            <zfin:link entity="${lab.organization}"/>
            <%--${lab.currentLineDesignation ? "": "<span style='font-size:small;'>*</span>"}--%>
            ${!status.last ? "," : ""}
            <%--</c:if>--%>
        </c:forEach>
    </c:if>
    <%--${formBean.hasNonCurrentLabs ? "<br><span style='font-size:small;'>* Line designation is no longer active for this lab.</span>": ""}--%>
</authz:authorize>

<c:choose>
    <c:when test="${empty formBean.featureLabEntries}">
        <div style="color:gray;">
            No features associated with this prefix at this time.
        </div>
    </c:when>
    <c:otherwise>
        <c:if test="${formBean.featureLabEntries.size() < 500}">
        <br/>
        <table class="summary rowstripes">
            <tr>
                <th width="10%">Allele</th>
                <th width="10%">Type</th>
                <th width="10%">Affected Gene</th>
                <th width="10%">Construct</th>
                <th width="10%">Lab of Origin</th>
            </tr>
            <c:forEach var="feature" items="${formBean.featureLabEntries}" varStatus="loop">
                <tr class=${loop.index%2==0 ? "even" : "odd"}>
                    <td>
                        <zfin:link entity="${feature.feature}"/>
                    </td>
                    <td>
                            ${feature.feature.type.display}
                    </td>
                    <td>
                        <c:forEach var="gene" items="${feature.feature.featureMarkerRelations}">
                            <li style="list-style-type: none;">
                                <c:if test="${gene.featureMarkerRelationshipType.affectedMarkerFlag eq 'true'}">
                                    <a href="/${gene.marker.zdbID}"> <i>${gene.marker.abbreviation}</i></a>
                                </c:if>
                            </li>
                        </c:forEach>
                    </td>
                    <td>
                        <c:forEach var="construct" items="${feature.feature.getConstructs()}">
                            <li style="list-style-type: none;">
                                <a href="/${construct.marker.zdbID}"> ${construct.marker.abbreviation}</a>
                            </li>
                        </c:forEach>
                    </td>
                    <td>
                            <zfin:link entity="${feature.sourceOrganization}"/>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
        <c:if test="${formBean.featureLabEntries.size() > 500}">
        <ul>
            <c:forEach var="featureLabEntry" items="${formBean.featureLabEntries}">
                <li><zfin:link entity="${featureLabEntry.feature}"/>
                    <c:if test="${!featureLabEntry.current && !empty featureLabEntry.sourceOrganization}">
                        (<zfin:link entity="${featureLabEntry.sourceOrganization}"/>)
                    </c:if>
                </li>
            </c:forEach>
        </ul>
        </c:if>
    </c:otherwise>

</c:choose>

