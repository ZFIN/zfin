<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>



<z:dataTable collapse="true" hasData="${!empty formBean.proteinDetailDomainBean.interProDomains}">
    <c:if test="${!fn:contains(formBean.marker.zdbID,'RNAG')}">
        <thead>
            <tr>
                <th>Protein</th>
                <th class="text-right">Length</th>
                <c:forEach var="category" items="${formBean.proteinType}">
                    <th>${category}</th>
                </c:forEach>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="category" items="${formBean.proteinDetailDomainBean.interProDomains}">
                <tr>
                    <td class="text-nowrap">
                        <zfin:link entity="${category.proDBLink}"/>
                        <z:otherPagesDropdown>
                            <zfin2:externalLink
                                    className="dropdown-item"
                                    href="https://www.ebi.ac.uk/interpro/protein/UniProt/${category.proDBLink.accessionNumber}"
                            >
                                InterPro
                            </zfin2:externalLink>
                            <c:if test="${category.PDB}">
                                <zfin2:externalLink
                                        className="dropdown-item"
                                        href="https://www.rcsb.org/pdb/protein/${category.proDBLink.accessionNumber}"
                                >
                                    PDB
                                </zfin2:externalLink>
                            </c:if>
                        </z:otherPagesDropdown>
                    </td>
                    <td class="text-right">${category.proDBLink.length}</td>
                    <c:forEach var="entry" items="${category.interProDomain}">
                        <td>
                            <c:if test="${fn:contains(entry.value, 'X')}">
                                <i class="fas fa-check"></i>
                            </c:if>
                        </td>
                    </c:forEach>
                </tr>
            </c:forEach>
        </tbody>
    </c:if>
</z:dataTable>