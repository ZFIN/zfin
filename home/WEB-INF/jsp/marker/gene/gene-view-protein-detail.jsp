<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>



<z:dataTable collapse="true" hasData="${!empty formBean.proteinDetailDomainBean.interProDomains}">
    <c:if test="${!fn:contains(formBean.marker.zdbID,'RNAG')}">
        <thead>
            <tr>
                <th>Protein</th>
                <th>Length</th>
                <c:forEach var="category" items="${formBean.proteinType}">
                    <th>${category}</th>
                </c:forEach>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="category" items="${formBean.proteinDetailDomainBean.interProDomains}">
                <tr>
                    <%--<td>${category.proDetail.upID}</td>
                    <td>${category.proDetail.upLength}</td>--%>

                    <td> <zfin:link entity="${category.proDBLink}"/><br> <zfin2:externalLink
                            href="https://www.ebi.ac.uk/interpro/protein/UniProt/${category.proDBLink.accessionNumber}">InterPro</zfin2:externalLink>
                            <c:if test="${category.PDB}"><zfin2:externalLink href="https://http://www.rcsb.org/pdb/protein/${category.proDBLink.accessionNumber}">, PDB</zfin2:externalLink> </c:if>

                    </td>
                            <td>${category.proDBLink.length}</td>
                    <c:forEach var="entry" items="${category.interProDomain}">
                        <td style="padding-center: 1em;">
                            <c:if test="${fn:contains(entry.value,'X')}">
                                <i class="fas fa-check"></i>
                            </td>
                            </c:if>
                    </c:forEach>
                </tr>
            </c:forEach>
        </tbody>
    </c:if>
</z:dataTable>