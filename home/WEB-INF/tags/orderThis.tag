<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="organization" type="org.zfin.people.Organization" required="false" %>
<%@ attribute name="markerSuppliers" type="java.util.Collection" required="false" %>
<%@ attribute name="accessionNumber" type="java.lang.String" required="true" %>


<c:choose>
    <%-- display order this link only --%>
    <c:when test="${organization != null}">
        <c:forEach var="url" items="${organization.organizationUrls}">
            <c:if test="${url.businessPurpose eq 'order'}">
                &nbsp;(<span style="font-size: smaller;"><a href="${url.urlPrefix}${accessionNumber}">${url.hyperlinkName}</a></span>)
            </c:if>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <c:forEach var="markerSupplier" items="${markerSuppliers}">
            <a href="${markerSupplier.organization.url}"
               id="${markerSupplier.organization.zdbID}"> ${markerSupplier.organization.name}</a>
            <c:forEach var="url" items="${markerSupplier.organization.organizationUrls}">
                <c:if test="${url.businessPurpose eq 'order'}">
                    &nbsp;(<span style="font-size: smaller;"><a href="${url.urlPrefix}${accessionNumber}">${url.hyperlinkName}</a></span>)
                </c:if>
            </c:forEach>
        </c:forEach>
    </c:otherwise>
</c:choose>
