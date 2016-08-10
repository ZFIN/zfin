<%@ page import="org.hibernate.stat.SessionStatistics" %>
<%@ page import="org.hibernate.stat.internal.SessionStatisticsImpl" %>
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

</body>
</html>
