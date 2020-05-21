<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataList hasData="${!empty formBean.pathwayDBLinks}">
    <c:forEach var="link" items="${formBean.pathwayDBLinks}">
        <li>
            <a href="${link.link}">${link.referenceDatabaseName}</a>
        </li>
    </c:forEach>
</z:dataList>