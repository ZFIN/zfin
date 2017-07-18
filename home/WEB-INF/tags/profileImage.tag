<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ attribute name="value" type="org.zfin.profile.HasImage" %>
<%@ attribute name="className" type="java.lang.String" required="false" %>

<c:choose>
    <c:when test="${!empty value.image}">
        <img class="${className}" src="<%=ZfinPropertiesEnum.IMAGE_LOAD.value()%>/${value.image}">
    </c:when>
    <c:otherwise>
        <img class="${className}" src="/images/LOCAL/smallogo.gif">
    </c:otherwise>
</c:choose>

