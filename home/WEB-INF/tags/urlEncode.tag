<%@ tag import="java.net.URLEncoder" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="string" required="true" rtexprvalue="true" %>
<%= URLEncoder.encode(string) %>
