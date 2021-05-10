<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:dataTable collapse="true"
             hasData="${!empty formBean.genomicFeatures}">
    <thead>
    <tr>
        <th>Genomic Feature</th>
        <th>Affected Genomic Regions</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="feature" items="${formBean.genomicFeatures}" varStatus="loop">
      <tr>
        <td>
            <zfin:link entity="${feature}"/>
        </td>
        <td>
            <zfin:link entity="${feature.affectedGenes}"/>
        </td>
      </tr>
    </c:forEach>
    </tbody>

    </div>
</z:dataTable>