<%@ page import="org.hibernate.stat.SessionStatistics" %>
<%@ page import="org.hibernate.stat.SessionStatisticsImpl" %>
<%@ page import="org.zfin.framework.HibernateUtil" %>
<%@ page language="java" %>
<%@ page session="false" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>


<!DOCTYPE html>

<!-- Server: <%=ZfinProperties.getInstance()%> -->


<%-- Screws up superscripts in links (check feature pages to fix) --%>
<%--<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">--%>

<%--
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
--%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Cache-Control" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>
    <title>
        <%--<tiles:getAsString ignore="true" name="staticTitle"/> ${dynamicTitle}--%>
        <tiles:getAsString ignore="true" name="staticTitle"/> ${dynamicTitle}
    </title>

    <link href="/css/font-awesome.min.css" rel="stylesheet">

    <link rel=stylesheet type="text/css" href="/css/searchresults.css">
    <link rel=stylesheet type="text/css" href="/css/summary.css">
    <link rel=stylesheet type="text/css" href="/css/Lookup.css">
    <link rel=stylesheet type="text/css" href="/css/datapage.css">
    <link rel=stylesheet type="text/css" href="/css/popup.css">
    <link rel=stylesheet type="text/css" href="/css/tipsy.css">
    <link rel=stylesheet type="text/css" href="/css/jquery.modal.css">
    <link rel=stylesheet type="text/css" href="/css/typeahead.css">
    <link rel="stylesheet" href="/css/datepicker3.css">

    <script src="/javascript/header.js" type="text/javascript"></script>
    <script type="text/javascript" src="/javascript/jquery.modal.min.js"></script>
    <script type="text/javascript" src="/javascript/jquery.tipsy.js"></script>
    <script type="text/javascript" src="/javascript/sorttable.js"></script>

    <script>
        if (hdrGetCookie("tabCookie") === "Motto") {
            hdrSetCookie("tabCookie", "Research", "", "/");
        }
    </script>

</head>

<a name="top"></a>

<tiles:insertAttribute name="header"/>

<tiles:insertAttribute name="body"/>

<tiles:insertAttribute name="footer"/>

<authz:authorize access="hasRole('root')">
    <zfin:printDebugInfo>
        <table cellpadding=5 width=100%>
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
            <c:forEach var="session" items="${requestScope}">
                <tr>
                    <td class="listContentBold">
                        <c:out value='${session.key}'/>
                    </td>
                    <td class="listContent">
                        <c:out value='${session.value}'/>
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
            <c:forEach var="session" items="${sessionScope}">
                <tr>
                    <td class="listContentBold">
                        <c:out value='${session.key}'/>
                    </td>
                    <td class="listContent">
                        <c:out value='${session.value}'/>
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
            <c:forEach var="session" items="${pageScope}">
                <tr>
                    <td class="listContentBold">
                        <c:out value='${session.key}'/>
                    </td>
                    <td class="listContent">
                        <c:out value='${session.value}'/>
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
            <c:forEach var="session" items="${applicationScope}">
                <tr>
                    <td class="listContentBold">
                        <c:out value='${session.key}'/>
                    </td>
                    <td class="listContent">
                        <c:out value='${session.value}'/>
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
            <c:forEach var="session" items="${param}">
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
