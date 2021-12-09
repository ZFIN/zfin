<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>
<c:set var="genotype" value="${formBean.genotype}"/>

<z:dataTable collapse="true"
             hasData="${formBean.genotypeFeatures != null && fn:length(formBean.genotypeFeatures) > 0 }">
    <thead>
        <tr>
            <th>Genomic Feature</th>
            <th>Construct</th>
            <th>Lab of Origin</th>
            <th>Zygosity</th>
            <th>Parental Zygosity</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="genoFeat" items="${formBean.genotypeFeatures}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${genoFeat.feature}"/>
                </td>
                <td>
                    <c:forEach var="construct" items="${genoFeat.feature.constructs}"
                               varStatus="constructsloop">

                        <a href="/${construct.marker.zdbID}"><i>${construct.marker.name}</i></a><c:if
                            test="${!constructsloop.last}">,&nbsp;</c:if>
                    </c:forEach>
                </td>
                <td>
                    <c:forEach var="source" items="${genoFeat.feature.sources}" varStatus="status">
                        <c:if test="${source.organization.zdbID != 'ZDB-LAB-000914-1'}">
                            <a href="/${source.organization.zdbID}">
                                    ${source.organization.name}
                            </a>
                        </c:if>
                        <c:if test="${!status.last}">,&nbsp;</c:if>
                    </c:forEach>
                </td>
                <td>
                        ${genoFeat.zygosity.name}
                </td>
                <td>
                        ${genoFeat.parentalZygosityDisplay}
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </tbody>
</z:dataTable>