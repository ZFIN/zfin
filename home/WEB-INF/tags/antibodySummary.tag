<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="antibodyBeans" type="java.util.Collection" required="true" %>

<%-- Should always have atleast one sequence, so won't ever hide --%>
<zfin2:subsection title="ANTIBODIES" showNoData="true" showEditLink="false" test="${!empty antibodyBeans}">
    <table class="summary rowstripes">
        <tr>
            <th width="17%">Name</th>
            <th width="17%"> Type</th>
            <th width="10%">Isotype</th>
            <th width="17%"> Host Organism</th>
            <th width="17%"> Assay</th>
            <th width="5%"> Publication</th>
            <th width="17%"> Source</th>
        </tr>
        <c:forEach var="antibodyBean" items="${antibodyBeans}" varStatus="loop">
            <c:set var="antibody" value="${antibodyBean.antibody}"/>
            <tr class=${loop.index%2==0 ? "even" : "odd"}>
                <td>
                    <zfin:link entity="${antibody}"/>
                </td>
                <td>
                        ${antibody.clonalType}
                </td>
                <td>
                        ${antibody.heavyChainIsotype}
                    <c:if
                            test="${antibody.heavyChainIsotype != null && antibody.lightChainIsotype != null}">,
                    </c:if>
                        ${antibody.lightChainIsotype}
                </td>
                <td>
                        ${antibody.hostSpecies}
                </td>
                <td>
                    <c:forEach var="assay" items="${antibody.distinctAssayNames}" varStatus="loop">
                        ${assay}${!loop.last ? ", " : ""}
                    </c:forEach>
                </td>
                <td style="vertical-align: text-top">
                    <div class="summary">
                        <a href="/action/antibody/antibody-publication-list?antibodyID=${antibodyBean.antibody.zdbID}&orderBy=author">${antibodyBean.numPubs}</a>
                    </div>
                </td>
                <td><zfin2:orderThis markerSuppliers="${antibodyBean.antibody.suppliers}"
                                     accessionNumber="${antibodyBean.antibody.zdbID}"/></td>
            </tr>
        </c:forEach>
    </table>
</zfin2:subsection>

