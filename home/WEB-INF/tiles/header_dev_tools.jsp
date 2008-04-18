<%@ page import="org.zfin.framework.presentation.LoginController"%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width=100% cellspacing=0 border=0 cellpadding=0 class="header">
    <tr>
        <td rowspan=2 bgcolor="#006666" width=80 align=left valign=bottom>
            <a href="/">
                <img src="/images/zfinlogo.gif" border=0 alt="logo">
            </a>
        </td>

        <td>
            <a href="/"> <img src="/images/zfintxt.gif" border=0 alt="logo"></a>
        </td>
        <td align="center" valign="bottom" bgcolor="#FFFFFF">

        </td>
    </tr>

    <tr bgcolor=#006666>
        <td colspan=2 width=100%>
            <table width=100% border=0 height=20 cellspacing=0 cellpadding=0>

                <tr>
                    <td align=center>
                        <DIV class="header">
                            <A HREF="/">Home</a>
                        </DIV>
                    </td>
                    <td align=center>
                        <DIV class="header">
                            <A HREF="/action/dev-tools/home">Dev Tools</a>
                        </DIV>
                    </td>
                    <td align=center>
                        <DIV class="header">
                            <A HREF="/action/<%= LoginController.LOGOUT%>">Logout</a>
                        </DIV>
                    </td>

                    <td align=center>
                        <DIV class="header">
                        </DIV>
                    </td>
                    <td align=center>
                        <DIV class="header">
                        </DIV>
                    </td>
                    <td align=center>
                        <DIV class="header">
                        </DIV>
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