<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="label" rtexprvalue="true" required="false"
              description="if nothing is specified, Synonyms: will be used" %>

<c:if test="${empty label}">
    <c:set var="label" value="Synonyms:"/>
</c:if>

<c:if test="${!empty previousNames}">
    <tr>
        <th>
                ${label}
        </th>
        <td>
            <c:forEach  var="markerAlias" items="${previousNames}" varStatus="loop">
                <span id="${markerAlias.alias}">${markerAlias.linkWithAttribution}</span>${(!loop.last ?", " : "")}
            </c:forEach>
        </td>
    </tr>
</c:if>



