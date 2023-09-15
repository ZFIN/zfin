<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataList hasData="${!empty formBean.pathwayDBLinks}">
    <c:forEach var="link" items="${formBean.pathwayDBLinks}">
        <li>
            <zfin2:externalLink href="${link.link}">${link.referenceDatabaseName}</zfin2:externalLink>
        </li>
    </c:forEach>
</z:dataList>