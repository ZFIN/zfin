<%@ page import="org.springframework.security.core.Authentication" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.web.WebAttributes" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<h1>Sorry, access is denied</h1>


<p>
<%= request.getAttribute(WebAttributes.ACCESS_DENIED_403)%>

<p>

<%		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) { %>
			Authentication object as a String: <%= auth.toString() %><BR><BR>
<%      } %>
