<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hasImage" type="java.lang.Boolean" required="true" %>

<c:if test="${hasImage}">
    <img src="/images/camera_icon.gif" alt="with image" image="" border="0">
</c:if>

