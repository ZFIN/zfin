<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="list" required="true" rtexprvalue="true" type="java.util.Collection"
              description="collection to be used" %>
<%@ attribute name="singular" required="true" type="java.lang.String" description="Name for one" %>
<%@ attribute name="nonSingular" required="true" type="java.lang.String" description="Name for one" %>

<c:choose>
    <c:when test="${fn:length(list) ne null && fn:length(list) > 1}">
        ${nonSingular}
    </c:when>
    <c:otherwise>
        ${singular}
    </c:otherwise>
</c:choose>