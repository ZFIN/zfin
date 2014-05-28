<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%--note: can not use this, creates another global bean, but improperly initialized--%>
<%--<jsp:useBean id="formBean" class="org.zfin.framework.presentation.ZfinSessionBean"/>--%>

<table cellpadding="2" cellspacing="1" border="0" width="79%">

    <tr class="odd"><td colspan="3" class="sectionTitle">
        ${formBean.numberOfGlobalSessions} active Sessions
        <em>(Current: <a href="/action/dev-tools/view-session-info">${pageContext.session.id}</a>)</em></td></tr>
    <%--<tr>--%>
    <%--<td colspan="3">--%>
    <%--<table cellpadding="2" cellspacing="5" border="0" width="100%">--%>
    <%--<tr class="odd">--%>
    <%--<th>ID  </th>--%>
    <%--<th>Name</th>--%>
    <%--<th>Value</th>--%>
    <%--</tr>--%>
    <%--<c:forEach var="session" items="${formBean.sessionRegistry}" varStatus="rowIndex">--%>
    <%--<c:set var="lastKey" value=""/>--%>
    <%--<c:forEach var="v" items="${session.value}">--%>
    <%--<tr class="${session.key ne lastKey ? "odd" : ""}">--%>
    <%--<td class="listContent">--%>
    <%--<em>${session.key ne lastKey ? session.key : "&nbsp;"}</em>--%>
    <%--</td>--%>
    <%--<td class="listContent">--%>
    <%--${v.key}--%>
    <%--</td>--%>
    <%--<td class="listContent">--%>
    <%--${v.value}--%>
    <%--</td>--%>
    <%--</tr>--%>
    <%--<c:set var="lastKey" value="${session.key}"/>--%>
    <%--</c:forEach>--%>
    <%--</c:forEach>--%>
    <%--</table>--%>
    <%--</td>--%>
    <%--</tr>--%>
</table>

All principals
<table border="1">
    <c:forEach var="principal" items="${formBean.principals}">
        <tr valign="top">
            <td width="30%">${principal.principal}
                <ul>
                    <c:forEach var="sessionAttribute" items="${formBean.sessionAttributes}">
                        <li> ${sessionAttribute} </li>
                    </c:forEach>
                </ul>
            </td>
            <td>
                <ul>
                    <c:forEach var="session" items="${principal.sessionList}">
                    <li><zfin2:sessionInformation session="${session}" currentSession="${pageContext.session.id}"/>
                        </c:forEach>
                </ul>
            </td>
        </tr>
    </c:forEach>
</table>
