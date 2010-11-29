<%@ page import="java.util.Date" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.framework.presentation.ZfinSessionBean" scope="request" />

<table cellpadding="2" cellspacing="1" border="0" width="50%">

    <tr><td colspan="3" class="sectionTitle">Request/Session Information</td></tr>
    <tr>
        <td width="100" class="sectionTitle">Request Attribute Key</td>
        <td class="sectionTitle">Request Attribute Value</td>
    </tr>
    <c:forEach var="session" items="${formBean.requestAttributes}">
        <tr>
            <td class="listContentBold">
                <c:out value='${session.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${session.value}'/>
            </td>
        </tr>
    </c:forEach>
    <tr class="bold">
        <td width="100" colspan="2" class="sectionTitle">Individual Session Info</td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Session ID: </td>
        <td colspan="2" class="listContent">
            <%= request.getSession().getId() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Session Expiration Time: </td>
        <td colspan="2" class="listContent">
            <%= request.getSession().getMaxInactiveInterval() %> seconds
            (<%= request.getSession().getMaxInactiveInterval()/60 %> minutes) 
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Date of Session Creation: </td>
        <td colspan="2" class="listContent">
            <%= new Date(request.getSession().getCreationTime()) %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Date last accessed Session: </td>
        <td colspan="2" class="listContent">
            <%= new Date(request.getSession().getLastAccessedTime()) %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Session size: </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.sessionSize}" />
        </td>
    </tr>
    <tr>
        <td width="100" colspan="2" class="sectionTitle">Session Contents</td>
    </tr>
    <c:forEach var="session" items="${formBean.sessionAttributes}">
        <tr>
            <td class="listContentBold">
                <c:out value='${session.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${session.value}'/>
            </td>
        </tr>
    </c:forEach>

</table>
