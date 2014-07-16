<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="items" type="java.util.Collection" rtexprvalue="true" required="true" %>
<%@attribute name="delimiter" type="java.lang.String" rtexprvalue="true" required="false" %>

<c:set var="delimiter" value="${empty delimiter ? ', ' : delimiter}" />

<c:forEach var="item" items="${items}" varStatus="loop">
    <zfin:link entity="${item}"/>
    ${loop.last ? "" : delimiter}
</c:forEach>
