<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataList hasData="${!empty formBean.plasmidDBLinks}">
    <c:forEach var="link" items="${formBean.plasmidDBLinks}">
        <li>
            <zfin2:externalLink href="${link.link}">${link.referenceDatabaseName}:${link.accNumDisplay}</zfin2:externalLink>
        </li>
    </c:forEach>
</z:dataList>
