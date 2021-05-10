<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable collapse="true" hasData="${!empty formBean.ipProtein}">
    <c:if test="${!fn:contains(formBean.marker.zdbID,'RNAG')}">
        <thead>
            <tr>
                <th style="width: 17%">Type</th>
                <th style="width: 17%">InterPro ID</th>
                <th style="width: 17%">Name</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="category" items="${formBean.ipProtein}">
                <tr>
                    <td>${category.ipType}</td>
                    <td><zfin2:externalLink
                            href ="http://www.ebi.ac.uk/interpro/entry/${category.ipID}">${category.ipID}</zfin2:externalLink></td>
                    <td>${category.ipName}</td>
                </tr>
            </c:forEach>
        </tbody>
    </c:if>
</z:dataTable>

