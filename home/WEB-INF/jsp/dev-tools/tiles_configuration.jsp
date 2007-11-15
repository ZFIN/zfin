<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="50%">

    <tr><td colspan="3" class="sectionTitle">Tiles Configuration:</td></tr>
    <tr><td colspan="3" class="listContentBold">&nbsp;</td></tr>
    <tr>
        <td valign=top class="listContentBold">
            Tiles Configuration Files: </td>
        <td colspan="2" class="listContent">
            <c:forEach var="file" items="${tilesForm.configurationFileNames}">
                <a href="/action/dev-tools/file-content?fileName=<c:out value='${file}'/>" target="_fileContent">
                    <c:out value="${file}"/></a>
                <br/>
            </c:forEach>
        </td>
    </tr>
</table>
