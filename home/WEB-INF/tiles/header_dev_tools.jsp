<%@ page import="org.zfin.framework.presentation.LoginController"%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css"/>
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<style type="text/css">
    a.devtools:hover {
        color:white;
        text-decoration:underline;
    }
    a.devtools{
        color:white;
        text-decoration:none;
    }
</style>

<table width=100% cellspacing=0 border=0 cellpadding=0 class="header">
    <tr bgcolor=#006666>
        <td colspan=2 width=100%>
            <table width=100% border=0 height=20 cellspacing=0 cellpadding=0>

                <tr>
                    <td align=center>
                            <A class="devtools" HREF="/">Home</a>
                    </td>
                    <td align=center>
                            <A class="devtools" HREF="/action/devtool/home">Dev Tools</a>
                    </td>

                    <td align=center>
                            <A class="devtools" HREF="/action/devtool/log4j-configuration">Log4J</a>
                    </td>

                    <td align=center>
                            <A class="devtools" HREF="/action/<%= LoginController.LOGOUT%>">Logout</a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>

</table>
<table width="100%">
    <tr><td class="username">
        <zfin:username />
    </td></tr>
</table>
