<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="antibodyBean" type="org.zfin.marker.presentation.AntibodyMarkerBean"
              rtexprvalue="true" required="true" %>

<%@ attribute name="antibody" type="org.zfin.antibody.Antibody"
              rtexprvalue="true" required="true" %>

<%--providing b oth attributes for convenience--%>

<tr>
    <th>
        Host Organism:
    </th>
    <td>
        <span id="host-organism">${antibody.hostSpecies}</span>
    </td>
</tr>

<tr>
    <th>
        Immunogen Organism:
    </th>
    <td>
        <span id="immunogen-organism">${antibody.immunogenSpecies}</span>
    </td>
</tr>

<tr>
    <th>
        Isotype:
    </th>
    <td>
        ${antibody.heavyChainIsotype}<c:if
                test="${antibody.heavyChainIsotype != null && antibody.lightChainIsotype != null}">,
        </c:if>${antibody.lightChainIsotype}
    </td>
</tr>
<tr>
    <th>
        Type:
    </th>
    <td>
        <span id="clonal-type">${antibody.clonalType}</span>
    </td>
</tr>
<tr>
    <th>
        Assays:
    </th>
    <td>
        <c:forEach var="assay" items="${antibody.distinctAssayNames}" varStatus="loop">
            ${assay}${!loop.last ? ", " : ""}
        </c:forEach>
    </td>
</tr>
<tr>
    <th>
        Antigen&nbsp;Genes:
    </th>
    <td>
        <zfin2:toggledMarkerRelationshipList collection="${antibodyBean.antigenGenes}" maxNumber="4"/>
    </td>
</tr>
<tr>
    <th>
        Antibody Registry ID:
    </th>
    <td>
       <a href=http://antibodyregistry.org/search.php?q=${antibodyBean.abRegistryID}>${antibodyBean.abRegistryID}</a>
    </td>
</tr>
<tr>
    <th>
        Source:
    </th>
    <td>
        <zfin2:orderThis markerSuppliers="${antibodyBean.suppliers}" accessionNumber="${antibody.zdbID}"/>
    </td>
</tr>

