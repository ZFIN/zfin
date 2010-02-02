<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="entity" type="org.zfin.infrastructure.EntityAlias" rtexprvalue="true" required="true" %>

<c:if test="${fn:length(entity.aliases)>0}">
<div>
    <b>Alias:</b>
<c:forEach  var="markerAlias" items="${entity.aliases}" varStatus="loop">
    ${markerAlias.alias} <zfin:attribution entity="${markerAlias}"/><c:if test="${!loop.last}">, </c:if>
</c:forEach>
</div>
</c:if>



