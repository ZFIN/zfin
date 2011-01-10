<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<authz:authorize ifAllGranted="root">
    <a href="/zf_info/zfbook/lab_desig.html">Previous Static Lab Line Designations</a>
    <c:if test="${formBean.hasNonCurrentLabs}">
        Labs that have features using this designation but have an alternate designation:
        <c:forEach var="lab" items="${formBean.nonCurrentLabs}" varStatus="status">
            <%--<c:if test="${lab.lab.active}">--%>
            <zfin:link entity="${lab.lab}"/>
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

        <ul>
            <c:forEach var="featureLabEntry" items="${formBean.featureLabEntries}">
                <li><zfin:link entity="${featureLabEntry.feature}"/>
                    <c:if test="${!featureLabEntry.current && !empty featureLabEntry.sourceOrganization}">
                        (<zfin:link entity="${featureLabEntry.sourceOrganization}"/>)
                    </c:if>
                </li>
            </c:forEach>
        </ul>
    </c:otherwise>

</c:choose>

