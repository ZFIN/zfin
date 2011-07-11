<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:forEach var="geneProductBean" items="${formBean}">

    <table border=0 width=100% bgcolor=@HIGHLIGHT_COLOR@>
        <tr>
            <td>UniProt ID: ${geneProductBean.accession}</td>
        </tr>
        <tr>
            <td><KBD>${geneProductBean.comment}</KBD></td>
        </tr>
    </table>
</c:forEach>

<%--<c:if test="${fn:length(formBean)>0}">--%>
<table>
    <tr>
        <td>
            This information was provided by UniProt through a collaboration with ZFIN. (<a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=ZDB-PUB-020723-2">1</a>)
        </td>
    </tr>
</table>
<%--</c:if>--%>


