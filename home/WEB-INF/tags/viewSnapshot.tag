<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="value" type="org.zfin.profile.HasSnapshot" %>
<%@ attribute name="className" type="java.lang.String" required="false" %>

<c:choose>
    <c:when test="${!empty value.snapshot}">
        <img class="${className}" src="/action/profile/image/view/${value.zdbID}.jpg">
    </c:when>
    <c:otherwise>
        <img class="${className}" src="/images/LOCAL/smallogo.gif">
    </c:otherwise>
</c:choose>

