<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable collapse="true" hasData="${!empty formBean.antibodyBeans}">
    <thead>
        <tr>
            <th style="width: 17%">Name</th>
            <th style="width: 17%">Type</th>
            <th style="width: 10%">Isotype</th>
            <th style="width: 17%">Host Organism</th>
            <th style="width: 17%">Assay <a class="popup-link info-popup-link" href="/ZFIN/help_files/antibody_assay_help.html"></a></th>
            <th style="width: 17%">Source</th>
            <th style="width: 5%">Publications</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="antibodyBean" items="${formBean.antibodyBeans}">
            <c:set var="antibody" value="${antibodyBean.antibody}"/>
            <tr>
                <td><zfin:link entity="${antibody}"/></td>
                <td>${antibody.clonalType}</td>
                <td>
                        ${antibody.heavyChainIsotype}
                    <c:if test="${antibody.heavyChainIsotype != null && antibody.lightChainIsotype != null}">, </c:if>
                        ${antibody.lightChainIsotype}
                </td>
                <td>
                        ${antibody.hostSpecies}
                </td>
                <td>
                    <ul class="comma-separated">
                        <c:forEach var="gene" items="${antibody.distinctAssayNames}">
                            <li>${gene}</li>
                        </c:forEach>
                    </ul>
                </td>
                <td>
                    <zfin2:orderThis markerSuppliers="${antibody.suppliers}"
                                     accessionNumber="${antibody.zdbID}"
                                     organization=""/>
                </td>
                <td class="text-right">
                    <a href="/action/antibody/antibody-publication-list?antibodyID=${antibodyBean.antibody.zdbID}&orderBy=author">${antibodyBean.numPubs}</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</z:dataTable>