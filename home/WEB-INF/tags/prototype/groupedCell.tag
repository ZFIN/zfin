<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="items" required="true" rtexprvalue="true" type="java.util.Collection" %>
<%@ attribute name="loop" required="true" rtexprvalue="true" type="javax.servlet.jsp.jstl.core.LoopTagStatus" %>
<%@ attribute name="property" required="true" rtexprvalue="true" type="java.lang.String" %>

<c:set var="currentValue" value="${loop.current[property]}" />
<c:set var="previousValue" value="${items[loop.index - 1][property]}" />
<c:set var="newGroup" value="${loop.first || currentValue != previousValue}" />

<td class="${newGroup ? '' : 'border-top-0'}">
    <c:if test="${newGroup}">
        <jsp:doBody />
    </c:if>
</td>
