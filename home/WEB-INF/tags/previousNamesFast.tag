<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="label" rtexprvalue="true" required="false"
              description="if nothing is specified, Synonyms: will be used" %>

<c:if test="${empty label}">
    <c:set var="label" value="Synonym"/>
</c:if>

<c:choose>
    <c:when test="${fn:length(previousNames) > 1}">
        <c:set var="label" value="${label}s:"/>
    </c:when>
    <c:otherwise>
        <c:set var="label" value="${label}:"/>
    </c:otherwise>
</c:choose>

<c:if test="${!empty previousNames}">
    <tr>
        <th>
                ${label}
        </th>
        <td>
            <c:forEach var="markerAlias" items="${previousNames}" varStatus="loop">
                <span id="previous-name-${loop.index}">${markerAlias.linkWithAttribution}</span>
                                <span style="cursor: pointer;"
                                      ng-click="control.openPreviousNameEditor('${gene.zdbID}','${gene.name}', 'Gene Name')">
                    <i class="fa fa-pencil-square-o" aria-hidden="true" style="color: red"></i>
                    <i class="fa fa-trash" aria-hidden="true" style="color: red"></i>
                </span>
                ${(!loop.last ?", " : "")}
            </c:forEach>
        </td>
    </tr>
</c:if>



