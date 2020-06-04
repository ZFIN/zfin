<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="previousNames" required="true" type="java.util.List" %>

<z:attributeListItem label="Previous Names">
    <z:ifHasData test="${!empty previousNames}" noDataMessage="None">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${previousNames}">
                <li>${markerAlias.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:ifHasData>
</z:attributeListItem>
