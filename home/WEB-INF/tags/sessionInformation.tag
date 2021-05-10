<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="session" type="org.springframework.security.core.session.SessionInformation" required="true"  %>
<%@ attribute name="currentSession" type="java.lang.String" required="true"  %>

<%--${session.sessionId+"" eq currentSession ? "<b>"+session.sessionId +"</b>" : session.sessionId }--%>

<c:set var="thisSessionID" value="${session.sessionId}"/>
${thisSessionID eq currentSession ? "<b>current</b>" : "" }
${currentSession}
${session.lastRequest}
${session.expired ? "expired" : ""}

