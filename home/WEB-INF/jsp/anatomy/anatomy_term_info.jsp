<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.anatomy.AnatomyItem,
                 org.zfin.anatomy.DevelopmentStage" %>
<%@ page import="org.zfin.anatomy.presentation.StagePresentation" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<html>
<head>
    <!--css in zfin.css, but want don't want to apply all of the zfin styles-->
    <style type="text/css">
        a.external{
            background: transparent url(/images/external.png) no-repeat scroll right center ;
            padding-right: 13px;
        }

        a.close_link {
            color:#333333;
            font-family:sans-serif;
            font-weight:bold;
            text-decoration: none;
            font-size: Large;
        }
        body{
            font-family: arial, sans-serif;
        }
       

    </style>



</head>

<p></p>

<!-- Relationships -->
<TABLE width="100%">
    <tr valign=top>
        <td colspan=2 align="right" valign="center">
            <%--<a onclick="window.parent.useTerm('${formBean.anatomyItem.nameEscaped}'); " href="javascript:;">[Add To Search]</a>--%>
            <input type="button"  alt="Add To Search" value="Add To Search" onclick="window.parent.useTerm('${formBean.anatomyItem.nameEscaped}'); " >
            &nbsp;
            &nbsp;
            <a title="Close window" class="close_link" onclick="window.parent.hideTerm(); " href="javascript:">x</a>
        </td>
    </tr>
    <tr>
        <td width="80" valign=top>
            <FONT SIZE=+1><STRONG>Name:</STRONG></FONT></td>
        <td>
            <FONT SIZE=+1><STRONG>
             ${formBean.anatomyItem.name}
             <br>
             <a href="/action/anatomy/term-detail?anatomyItem.zdbID=${formBean.anatomyItem.zdbID}" class="external" target="_blank"><font size="-1">Anatomy Details</font></a>
            </STRONG></FONT>
            <c:if test="${formBean.anatomyItem.obsolete}"><span style="color:red">(obsolete)</span> </c:if>
        </td>
    </tr>
    <c:if test="${formBean.anatomyItem.synonyms != null  }">
        <tr valign="top">
            <td>
                Synonyms:
            </td>
            <td>
                ${formBean.formattedSynonymList}
            </td>
        </tr>
    </c:if>

    <c:if test="${formBean.anatomyItem.formattedDefinition != null  }">
        <tr valign="top">
            <td>
                Definition:
            </td>
            <td>
                ${formBean.anatomyItem.formattedDefinition}
            </td>
        </tr>
    </c:if>
    <TR>
        <TD bgcolor="#CCCCCC" colspan=2>
            <STRONG>Relationships</STRONG>
            (<a  class="external" href="/zf_info/ontology_relationship_info.html" target="_blank">about</a>)
        </TD>
    </TR>
    <c:forEach var="rt" items="${formBean.relations}">
        <TR>
            <TD bgcolor="#EEEEEE" valign=top align=left width=110>
                ${rt.type}:
            </TD>
            <TD bgcolor="#EEEEEE">
                <c:forEach var="session" items="${rt.items}">
                    <%--<zfin:link entity="${item}"/>--%>
                    <a href="/action/anatomy/term-info?anatomyItem.zdbID=${session.zdbID}">${session.name}</a>
                    &nbsp;
                </c:forEach>
            </TD>
        </TR>

    </c:forEach>
</TABLE>

<p>

<p>
<TABLE width="100%">
    <TR>
        <TD bgcolor="#CCCCCC">
            <STRONG>Appears at</STRONG>
        </TD>
        <TD bgcolor="#CCCCCC">
            <STRONG>Evident until</STRONG>
        </TD>
    </TR>
    <TR>
        <%
            AnatomyItem ai = formBean.getAnatomyItem();
            String startStage = StagePresentation.createDisplayEntry(ai.getStart());
            String endStage = StagePresentation.createDisplayEntry(ai.getEnd());
        %>
        <TD bgcolor="#EEEEEE">
            <%= startStage %>
        </TD>
        <TD bgcolor="#EEEEEE">
            <%= endStage %>
        </TD>
    </TR>
</TABLE>


</html>
