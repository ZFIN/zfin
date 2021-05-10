<%@ page import="java.util.Date"%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Browser Test">
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
                 ${pageContext.request.getHeader("host")}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Method: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.method}
            </td>
        </tr>
        <tr class="odd">
            <td valign=top class="listContentBold">
                Protocol: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.protocol}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Path: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.pathInfo}
            </td>
        </tr>
        <tr class="odd">
            <td valign=top class="listContentBold">
                Remote Address: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.remoteAddr}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Remote Host: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.remoteHost}
            </td>
        </tr>
        <tr class="odd">
            <td valign=top class="listContentBold">
                Remote Port: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.remotePort}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Remote User: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.remoteUser}
            </td>
        </tr>
        <tr class="odd">
            <td valign=top class="listContentBold">
                Channel: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.scheme}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Server Name: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.serverName}
            </td>
        </tr>
        <tr class="odd">
            <td valign=top class="listContentBold">
                Server Port: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.serverPort}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Servlert Path: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.servletPath}
            </td>
        </tr>
        <tr class="odd">
            <td valign=top class="listContentBold">
                Authorization Type: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.authType}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Is Requested Session ID from Cookie: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.requestedSessionIdFromCookie}
            </td>
        </tr>
        <tr class="odd">
            <td valign=top class="listContentBold">
                Is Requested Session ID from URL: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.requestedSessionIdFromURL}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Is Requested Session ID valid: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.requestedSessionIdValid}
            </td>
        </tr>
        <tr class="odd">
            <td valign=top class="listContentBold">
                Is Secure: </td>
            <td colspan="2" class="listContent">
                 ${pageContext.request.secure}
            </td>
        </tr>
        <tr>
            <td valign=top class="listContentBold">
                Create Session: </td>
            <td colspan="2" class="listContent">
                <jsp:useBean id="dateValue" class="java.util.Date"/>
                <jsp:setProperty name="dateValue" property="time" value="${pageContext.session.creationTime}"/>
                <fmt:formatDate value="${dateValue}" type="both" />
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
    </table>
</z:devtoolsPage>