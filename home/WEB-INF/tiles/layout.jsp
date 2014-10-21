<%@ page import="org.hibernate.stat.SessionStatistics" %>
<%@ page import="org.hibernate.stat.SessionStatisticsImpl" %>
<%@ page import="org.zfin.framework.HibernateUtil" %>
<%@ page language="java" %>
<%@ page session="false" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

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


    <link rel="stylesheet" type="text/css" href="/css/zfin.css">
    <c:if test="${empty suppressHeaderAndFooter || suppressHeaderAndFooter == false}">
        <link rel="stylesheet" type="text/css" href="/css/header.css">
    </c:if>
    <link rel="stylesheet" type="text/css" href="/css/footer.css">
    <link rel=stylesheet type="text/css" href="/css/searchresults.css">
    <link rel=stylesheet type="text/css" href="/css/summary.css">
    <link rel=stylesheet type="text/css" href="/css/spiffycorners.css">
    <link rel=stylesheet type="text/css" href="/css/Lookup.css">
    <link rel=stylesheet type="text/css" href="/css/datapage.css">
    <link rel=stylesheet type="text/css" href="/css/popup.css">
    <link rel=stylesheet type="text/css" href="/css/tipsy.css">
    <link rel=stylesheet type="text/css" href="/css/jquery.modal.css">


    <script src="/javascript/jquery-1.11.0.js" type="text/javascript"></script>


    <c:if test="${empty suppressHeaderAndFooter || suppressHeaderAndFooter == false}">
      <script src="/javascript/header.js" type="text/javascript"></script>
    </c:if>

    <script type="text/javascript" src="/javascript/jquery.modal.min.js"></script>
    <script type="text/javascript" src="/javascript/jquery.tipsy.js"></script>
    <script type="text/javascript" src="/javascript/sorttable.js"></script>

    <%-- todo: replace these with a local file?--%>
    <link href="//netdna.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">

    <script src="/javascript/jquery-ui-1.10.4.custom.js"></script>
    <link rel=stylesheet type="text/css" href="/css/jquery-ui-1.10.4.custom.css">




    <!-- Begin Inspectlet Embed Code -->
    <script type="text/javascript" id="inspectletjs">
        window.__insp = window.__insp || [];
        __insp.push(['wid', <%=ZfinProperties.getInspectletID()%>]);
        (function() {
            function __ldinsp(){var insp = document.createElement('script'); insp.type = 'text/javascript'; insp.async = true; insp.id = "inspsync"; insp.src = ('https:' == document.location.protocol ? 'https' : 'http') + '://cdn.inspectlet.com/inspectlet.js'; var x = document.getElementsByTagName('script')[0]; x.parentNode.insertBefore(insp, x); }
            if (window.attachEvent){
                window.attachEvent('onload', __ldinsp);
            }else{
                window.addEventListener('load', __ldinsp, false);
            }
        })();
    </script>
    <!-- End Inspectlet Embed Code —>




    <!-- Start GOOGLE Analytics -->
    <script>
        if ('@GOOGLE_ANALYTICS_ID@' != '0') {
            (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
                    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
            })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

            ga('create', '@GOOGLE_ANALYTICS_ID@', 'auto');
            ga('send', 'pageview');
        }
    </script>
    <!-- End GOOGLE Analytics -->

</head>

<a name="top"></a>

<tiles:insertAttribute name="header"/>

<tiles:insertAttribute name="body"/>

<tiles:insertAttribute name="footer"/>

<authz:authorize ifAnyGranted="root">
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
