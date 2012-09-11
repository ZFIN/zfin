<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="value" type="org.zfin.profile.HasSnapshot" %>

<c:choose>
    <c:when test="${!empty value.snapshot}">
        <img style="max-width: 500px;" src="/action/profile/image/view/${value.zdbID}.jpg">
    </c:when>
    <c:otherwise>
        <img style="max-width: 500px;" src="/images/LOCAL/smallogo.gif">
    </c:otherwise>
</c:choose>

