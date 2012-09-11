<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="member" required="true" type="org.zfin.profile.presentation.PersonMemberPresentation" %>
<%@ attribute name="suppressTitle" required="true" type="java.lang.Boolean" %>

<c:choose>
    <c:when test="${suppressTitle}">
        <a href="/${member.zdbID}">${member.name}</a>
    </c:when>
    <c:otherwise>
        <zfin:link entity="${member}"/>
    </c:otherwise>
</c:choose>
