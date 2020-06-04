<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="links" required="true" type="java.util.List" %>

<z:attributeListItem label="Genome Resources">
    <z:ifHasData test="${!empty links}" noDataMessage="None">
        <ul class="comma-separated">
            <c:forEach var="link" items="${links}">
                <li><a href="${link.link}">${link.displayName}</a> ${link.attributionLink}</li>
            </c:forEach>
        </ul>
    </z:ifHasData>
</z:attributeListItem>
