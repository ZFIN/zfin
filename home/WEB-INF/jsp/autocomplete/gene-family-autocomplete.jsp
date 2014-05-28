
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<ul>
<c:forEach var="markerFamily" items="${formBean.markerFamilyNames}">
    <li>${markerFamily.markerFamilyName}</li>    
</c:forEach>
</ul>