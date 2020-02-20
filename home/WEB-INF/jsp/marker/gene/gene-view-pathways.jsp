<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-prototype:dataTable hasData="${!empty formBean.pathwayDBLinks}">
    <c:forEach var="link" items="${formBean.pathwayDBLinks}" varStatus="loop">
        <tr>
            <td><a href="${link.link}">${link.referenceDatabaseName}</a></td>
        </tr>
    </c:forEach>
</zfin-prototype:dataTable>