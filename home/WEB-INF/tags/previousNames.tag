<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="entity" type="org.zfin.infrastructure.EntityAlias" rtexprvalue="true" required="true" %>
<%@ attribute name="label" rtexprvalue="true" required="false"
              description="if nothing is specified, Synonyms: will be used" %>

<c:if test="${empty label}">
    <c:set var="label" value="Synonyms:"/>
</c:if>

<c:if test="${fn:length(entity.aliases)>0}">
<tr>
    <th>
        ${label}
    </th>
    <td>
        <c:forEach  var="markerAlias" items="${entity.aliases}" varStatus="loop"><span id="${markerAlias.alias}">${markerAlias.alias}</span><zfin:attribution entity="${markerAlias}"/><c:if test="${!loop.last}">, </c:if></c:forEach>
    </td>
</tr>
</c:if>



