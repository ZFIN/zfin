<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="label" required="true" rtexprvalue="true" %>
<%@ attribute name="link" required="false" rtexprvalue="true" %>

<c:choose>
    <c:when test="${link != null}">
        <dt class="col-sm-2 mb-sm-2"><a href="${link}">${label}</a></dt>
    </c:when>
    <c:otherwise>
        <dt class="col-sm-2 mb-sm-2">${label}</dt>
    </c:otherwise>
</c:choose>
<dd class="col-sm-10"><jsp:doBody /></dd>
