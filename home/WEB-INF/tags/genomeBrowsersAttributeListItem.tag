<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="locations" type="java.util.Collection" required="true" %>

<z:attributeListItem label="Genome Browsers">
    <z:ifHasData test="${!empty locations}">
        <ul class="list-inline mb-0">
            <c:forEach var="location" items="${locations}">
                <c:set var="isExternal" value="${not fn:startsWith(location.url, '/')}"/>
                <a href="${location.url}" class="list-inline-item ${isExternal ? 'external' : ''}">${location.name}</a>
            </c:forEach>
        </ul>
    </z:ifHasData>
</z:attributeListItem>