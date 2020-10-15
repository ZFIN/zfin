<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="previousNames" required="true" type="java.util.List" %>
<%@ attribute name="name" required="false" type="java.lang.String"  %>

<c:set var="title" value="${(empty name) ? 'Previous Name' : name}" />

<z:attributeListItem label="${title}">
    <z:ifHasData test="${!empty previousNames}" noDataMessage="None">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${previousNames}" varStatus="loop">
                <li id="previous-name-${loop.index}">${markerAlias.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:ifHasData>
</z:attributeListItem>
