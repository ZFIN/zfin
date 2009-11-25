<%@ page import="org.zfin.framework.presentation.LoginController"%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width=100% cellspacing=0 border=0 cellpadding=0 class="header">
    <tr>
        <td rowspan=2 bgcolor="#006666" width=80 align=left valign=bottom>
  <a href="/">
   <img id="logo-img" src="/images/zfinlogo.png">
   <img id="logo-text" src="/images/zfintxt.png">
  </a>
        </td>

        <td>
   <img id="logo-text" src="/images/zfintxt.png">
        </td>
        <td align="center" valign="bottom" bgcolor="#FFFFFF">

        </td>
    </tr>

<%--       a:hover { --%>
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

    <tr bgcolor=#006666>
        <td colspan=2 width=100%>
            <table width=100% border=0 height=20 cellspacing=0 cellpadding=0>

                <tr>
                    <td align=center>
                            <A class="devtools" HREF="/">Home</a>
                    </td>
                    <td align=center>
                            <A class="devtools" HREF="/action/dev-tools/home">Dev Tools</a>
                    </td>

                    <td align=center>
                            <A class="devtools" HREF="/action/dev-tools/log4j-configuration">Log4J</a>
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
