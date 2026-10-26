<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="links" required="true" type="java.util.List" %>

<z:attributeListItem label="Genome Resources">
    <z:ifHasData test="${!empty links}" noDataMessage="None">
        <%-- Secondary links (sorted last by the controller) go behind the list-collapse "(all N)" toggle. --%>
        <c:set var="primaryCount" value="0"/>
        <c:set var="hasSecondary" value="false"/>
        <c:forEach var="link" items="${links}">
            <c:choose>
                <c:when test="${link.secondary}"><c:set var="hasSecondary" value="true"/></c:when>
                <c:otherwise><c:set var="primaryCount" value="${primaryCount + 1}"/></c:otherwise>
            </c:choose>
        </c:forEach>
        <ul class="comma-separated" ${hasSecondary ? 'data-toggle="collapse"' : ''} data-show="${primaryCount}">
            <c:forEach var="link" items="${links}">
                <li><a href="${link.link}">${link.displayName}</a> ${link.attributionLink}</li>
            </c:forEach>
        </ul>
    </z:ifHasData>
</z:attributeListItem>
