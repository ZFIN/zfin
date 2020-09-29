<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="links" required="true" type="java.util.List" %>

<z:attributeListItem label="RNA Central">
    <z:ifHasData test="${!empty links}" noDataMessage="None">
        <ul class="comma-separated">
            <c:forEach var="link" items="${links}">

                <c:if test="${link.displayName.contains('URS')}">

                <li><a href="${link.link}">${link.displayName}</a> ${link.attributionLink}</li>
                </c:if>
            </c:forEach>
        </ul>
    </z:ifHasData>
</z:attributeListItem>
