<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@ attribute name="to" type="java.lang.String" %>
<%@ attribute name="subject" type="java.lang.String" %>

<c:if test="${empty to}">
    <c:set var="to" value="${ZfinPropertiesEnum.ZFIN_ADMIN.value()}" />
</c:if>

<jsp:doBody var="body" />

<a href="mailto:${to}${empty subject ? '' : '?subject='}${subject}">${empty body ? to : body}</a>