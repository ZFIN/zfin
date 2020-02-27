<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable hasData="${!empty formBean.plasmidDBLinks}">
    <c:forEach var="link" items="${formBean.plasmidDBLinks}" varStatus="loop">
        <tr>
            <td><a href="${link.link}">${link.referenceDatabaseName}:${link.accNumDisplay}</a></td>
        </tr>
    </c:forEach>
</z:dataTable>