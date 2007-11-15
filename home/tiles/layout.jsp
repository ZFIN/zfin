<%@ page import="org.hibernate.stat.SessionStatistics" %>
<%@ page import="org.hibernate.stat.SessionStatisticsImpl" %>
<%@ page import="org.zfin.framework.HibernateUtil" %>
<%@ page language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <meta http-equiv="Pragma" content="no-cache"/>
    <META HTTP-EQUIV="Content-type" CONTENT="text/html; charset=UTF-8"/>
    <title>
        <tiles:getAsString ignore="true" name="staticTitle"/> ${dynamicTitle}
    </title>

    <link type="text/css" rel="stylesheet" href="/css/dev_tools.css"/>

</head>

<a name="top"></a>

<tiles:insert attribute="header">
</tiles:insert>

<table cellpadding=5 width=100%>
    <tr>
        <td>
            <tiles:insert attribute="body">
                <tiles:put name="pageSection">
                    <tiles:getAsString ignore="true" name="pageSection"/>
                </tiles:put>
                <tiles:put name="pageSectionTitle">
                    <tiles:getAsString ignore="true" name="pageSectionTitle"/>
                </tiles:put>
            </tiles:insert>
        </td>
    </tr>
</table>

<tiles:insert attribute="footer">
</tiles:insert>

<authz:authorize ifAnyGranted="root">
<zfin:printDebugInfo ><table cellpadding=5 width=100%>
    <tr>
        <td>
            <span class="xxsm-text">
            <br><br><br><br>
            JSP Page Name: 
            <tiles:getAsString name="body"/><br>
            <br>
            </span>
        </td>
    </tr>
    <tr>
        <td>
            <a href="/action/dev-tools/view-session-info"> Check your session</a>
        </td>
    </tr>
</table>

<table cellpadding=5 width=100%>
    <tr>
        <td colspan="3" class="sectionTitle">Session Info:</td>
    </tr>
    <tr>
        <td colspan="3" class="sectionTitle">Request Attributes:</td>
    </tr>
    <tr>
        <td width="100" class="sectionTitle">Key</td>
        <td class="sectionTitle">Value</td>
    </tr>
    <c:forEach var="item" items="${requestScope}">
        <tr>
            <td class="listContentBold">
                <c:out value='${item.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${item.value}'/>
            </td>
        </tr>
    </c:forEach>
    <tr>
        <td colspan="3" class="sectionTitle">Session Attributes:</td>
    </tr>
    <tr>
        <td width="100" class="sectionTitle">Key</td>
        <td class="sectionTitle">Value</td>
    </tr>
    <c:forEach var="item" items="${sessionScope}">
        <tr>
            <td class="listContentBold">
                <c:out value='${item.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${item.value}'/>
            </td>
        </tr>
    </c:forEach>
    <tr>
        <td colspan="3" class="sectionTitle">Page Attributes:</td>
    </tr>
    <tr>
        <td width="100" class="sectionTitle">Key</td>
        <td class="sectionTitle">Value</td>
    </tr>
    <c:forEach var="item" items="${pageScope}">
        <tr>
            <td class="listContentBold">
                <c:out value='${item.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${item.value}'/>
            </td>
        </tr>
    </c:forEach>
    <tr>
        <td colspan="3" class="sectionTitle">Application Attributes:</td>
    </tr>
    <tr>
        <td width="100" class="sectionTitle">Key</td>
        <td class="sectionTitle">Value</td>
    </tr>
    <c:forEach var="item" items="${applicationScope}">
        <tr>
            <td class="listContentBold">
                <c:out value='${item.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${item.value}'/>
            </td>
        </tr>
    </c:forEach>
    <tr>
        <td colspan="3" class="sectionTitle">Form Parameters:</td>
    </tr>
    <tr>
        <td width="100" class="sectionTitle">Key</td>
        <td class="sectionTitle">Value</td>
    </tr>
    <c:forEach var="item" items="${param}">
        <tr>
            <td class="listContentBold">
                <c:out value='${item.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${item.value}'/>
            </td>
        </tr>
    </c:forEach>
</table>

<%
    SessionStatistics stat = HibernateUtil.currentSession().getStatistics();
    SessionStatisticsImpl statImpl = null;
    if (stat instanceof SessionStatisticsImpl) {
        statImpl = (SessionStatisticsImpl) stat;
    }

%>
<table cellpadding=5 width=100%>
    <tr>
        <td colspan="3" class="sectionTitle">Hibernate Session Statistics:</td>
    </tr>
    <tr>
        <td width="100" class="sectionTitle">Key</td>
        <td class="sectionTitle">Value</td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Entity Mode:
        </td>
        <td colspan="2" class="listContent">
            <%= HibernateUtil.currentSession().getEntityMode()%>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Cache Mode:
        </td>
        <td colspan="2" class="listContent">
            <%= HibernateUtil.currentSession().getCacheMode()%>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Flush Mode:
        </td>
        <td colspan="2" class="listContent">
            <%= HibernateUtil.currentSession().getFlushMode()%>
        </td>
    </tr>
    <%--
            <tr>
                <td valign=top class="listContentBold">
                    Entity Keys: </td>
                <td colspan="2" class="listContent">
                    <%= stat.getEntityKeys()%>
                </td>
            </tr>
    --%>
    <%--
            <tr>
                <td valign=top class="listContentBold">
                    Entity Keys: </td>
                <td colspan="2" class="listContent">
                    <%= stat.getCollectionKeys()%>
                </td>
            </tr>
    --%>
    <tr>
        <td valign=top class="listContentBold">
            Impl:
        </td>
        <td colspan="2" class="listContent">
            <%= statImpl.toString()%>
        </td>
    </tr>


</table>
</zfin:printDebugInfo>
</authz:authorize>
</body>
</html>
