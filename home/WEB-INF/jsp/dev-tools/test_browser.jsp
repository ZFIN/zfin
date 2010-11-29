<%@ page import="java.util.Date"%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="75%">

    <tr class="search-result-table-header">
        <td colspan="2" class="sectionTitle">Test Browser: Request Information</td></tr>
    <tr>
        <td width="300" class="sectionTitle">Property Key</td>
        <td class="sectionTitle">Property value</td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Host: </td>
        <td colspan="2" class="listContent">
             <%= request.getHeader("host") %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Method: </td>
        <td colspan="2" class="listContent">
             <%= request.getMethod() %>
        </td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Protocol: </td>
        <td colspan="2" class="listContent">
             <%= request.getProtocol() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Path: </td>
        <td colspan="2" class="listContent">
             <%= request.getPathInfo() %>
        </td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Remote Address: </td>
        <td colspan="2" class="listContent">
             <%= request.getRemoteAddr() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Remote Host: </td>
        <td colspan="2" class="listContent">
             <%= request.getRemoteHost() %>
        </td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Remote Port: </td>
        <td colspan="2" class="listContent">
             <%= request.getRemotePort() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Remote User: </td>
        <td colspan="2" class="listContent">
             <%= request.getRemoteUser() %>
        </td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Channel: </td>
        <td colspan="2" class="listContent">
             <%= request.getScheme() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Server Name: </td>
        <td colspan="2" class="listContent">
             <%= request.getServerName() %>
        </td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Server Port: </td>
        <td colspan="2" class="listContent">
             <%= request.getServerPort() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Servlert Path: </td>
        <td colspan="2" class="listContent">
             <%= request.getServletPath() %>
        </td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Authorization Type: </td>
        <td colspan="2" class="listContent">
             <%= request.getAuthType() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Is Requested Session ID from Cookie: </td>
        <td colspan="2" class="listContent">
             <%= request.isRequestedSessionIdFromCookie() %>
        </td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Is Requested Session ID from URL: </td>
        <td colspan="2" class="listContent">
             <%= request.isRequestedSessionIdFromURL() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Is Requested Session ID valid: </td>
        <td colspan="2" class="listContent">
             <%= request.isRequestedSessionIdValid() %>
        </td>
    </tr>
    <tr class="odd">
        <td valign=top class="listContentBold">
            Is Secure: </td>
        <td colspan="2" class="listContent">
             <%= request.isSecure() %>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Create Session: </td>
        <td colspan="2" class="listContent">
             <%= new Date(request.getSession().getCreationTime()) %>
        </td>
    </tr>
    <tr class="search-result-table-header">
        <td colspan="2" class="sectionTitle">HTTP Header Information</td></tr>
    <c:forEach var="session" items="${form}">
        <tr>
            <td class="listContentBold">
                <c:out value='${session.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${session.value}'/>
            </td>
        </tr>
    </c:forEach>
s</table>
