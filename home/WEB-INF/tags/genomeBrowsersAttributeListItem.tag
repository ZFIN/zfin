<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="locations" type="java.util.Collection" required="true" %>
<%@ attribute name="omit" type="java.lang.String" required="false" %>
<c:set var="omitArray" value="${fn:split(omit, ',')}" />

<z:attributeListItem label="Genome Browsers">
    <z:ifHasData test="${!empty locations}">
        <ul class="list-inline mb-0">
            <c:forEach var="location" items="${locations}">
                <c:set var="shouldOmit" value="${zfn:arrayContains(omitArray, location.name)}"/>
                <c:if test="${!shouldOmit}">
                    <c:set var="isExternal" value="${not fn:startsWith(location.url, '/')}"/>
                    <a href="${location.url}" class="list-inline-item ${isExternal ? 'external' : ''}">${location.name}</a>
                </c:if>
            </c:forEach>
        </ul>
    </z:ifHasData>
</z:attributeListItem>