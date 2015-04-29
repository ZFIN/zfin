<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%--
Display a the entity as String (if it is) and add a suffix
If entity is not a string its assumed to be an EntityZdbID with a 'entityName' attribute.
--%>

<%@attribute name="entity" type="java.lang.Object" %>
<%@attribute name="delimiter" type="java.lang.String" required="false" %>
<%@attribute name="loop" type="javax.servlet.jsp.jstl.core.LoopTagStatus" %>

<c:choose>
    <c:when test="${entity.getClass().getSimpleName() eq 'String'}">${entity}${(!loop.last ? delimiter : "")}
    </c:when>
    <c:otherwise>
        ${entity.entityName}${(!loop.last ? delimiter : "")}
    </c:otherwise>
</c:choose>
