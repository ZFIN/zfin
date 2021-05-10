<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataList hasData="${!empty formBean.plasmidDBLinks}">
    <c:forEach var="link" items="${formBean.plasmidDBLinks}">
        <li>
            <a href="${link.link}">${link.referenceDatabaseName}:${link.accNumDisplay}</a>
        </li>
    </c:forEach>
</z:dataList>
