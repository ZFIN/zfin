<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable collapse="true" hasData="${!empty formBean.proteinDomainBeans}">
    <c:if test="${!fn:contains(formBean.marker.zdbID,'RNAG')}">
        <thead>
            <tr>
                <th style="width: 17%">Type</th>
                <th style="width: 17%">InterPro ID</th>
                <th style="width: 17%">Name</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="category" items="${formBean.proteinDomainBeans}">
                <tr>
                    <td>${category.ipType}</td>
                    <td><a href="http://www.ebi.ac.uk/interpro/entry/${category.ipID}">${category.ipID}</a></td>
                    <td>${category.ipName}</td>
                </tr>
            </c:forEach>
        </tbody>
    </c:if>
</z:dataTable>

<z:dataTable collapse="true" hasData="${!empty formBean.proteinDetailDomainBean}">
    <c:if test="${!fn:contains(formBean.marker.zdbID,'RNAG')}">
        <thead>
            <tr>
                <th style="width: 17%">Protein</th>
                <th style="width: 17%">Length</th>
                <c:forEach var="category" items="${formBean.proteinType}">
                    <th>${category}</th>
                </c:forEach>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="category" items="${formBean.proteinDetailDomainBean.interProDomains}">
                <tr>
                    <td>${category.proDetail.upID}</td>
                    <td>${category.proDetail.upLength}</td>
                    <c:forEach var="entry" items="${category.interProDomain}">
                        <td style="padding-right: 1em;"><b>${entry.value}</b></td>
                    </c:forEach>
                </tr>
            </c:forEach>
        </tbody>
    </c:if>
</z:dataTable>