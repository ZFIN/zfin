<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="popup-header">
    GENE PRODUCT DESCRIPTION
</div>

<c:forEach var="geneProductBean" items="${formBean}">

    <table border=0 width=100% bgcolor=#EEEEEE>
        <tr>
            <td>UniProt ID: <a href="https://www.uniprot.org/uniprot/${geneProductBean.accession}">${geneProductBean.accession}</a></td>
        </tr>
        <tr>
            <td><KBD>${geneProductBean.comment}</KBD></td>
        </tr>
    </table>
</c:forEach>

<table>
    <tr>
        <td>
            This information was provided by UniProt through a collaboration with ZFIN. (<a href="/ZDB-PUB-020723-2">1</a>)
        </td>
    </tr>
</table>


