<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.marker.presentation.CloneBean"
              rtexprvalue="true" required="true" %>

<c:set var="clone" value="${formBean.clone}"/>

<%--<%@ attribute name="isThisseProbe" type="java.lang.Boolean" rtexprvalue="true" required="true" %>--%>

<%-- this is intended to be a fragment of the primary entity attributes table called from within
cloneHead.tag ... that's why there's no table tag here, just rows --%>


<c:choose>
    <c:when test="${clone.rnaClone}">
        <z:attributeListItem label="Digest">
            ${clone.digest}
        </z:attributeListItem>

        <z:attributeListItem label="Insert Size">
            ${clone.insertSize}
        </z:attributeListItem>

        <z:attributeListItem label="Cloning Site">
            ${clone.cloningSite}
        </z:attributeListItem>
    </c:when>
    <c:otherwise>
        <z:attributeListItem label="Strain">
            <zfin:link entity="${clone.probeLibrary.strain}"/>
        </z:attributeListItem>

        <z:attributeListItem label="Sex">
            ${clone.probeLibrary.sex}
        </z:attributeListItem>

        <z:attributeListItem label="Tissue">
            <zfin:link entity="${clone.probeLibrary.tissue}"/>
        </z:attributeListItem>

        <z:attributeListItem label="Host">${clone.probeLibrary.host}</z:attributeListItem>
    </c:otherwise>
</c:choose>

<z:attributeListItem label="Vector Type">${clone.vector.type}</z:attributeListItem>

<z:attributeListItem label="Vector">${clone.vector.name}</z:attributeListItem>

<c:if test="${clone.rnaClone}">
    <z:attributeListItem label="Polymerase">${clone.polymeraseName}</z:attributeListItem>

    <z:attributeListItem label="PCR Amplification">${clone.pcrAmplification}</z:attributeListItem>
</c:if>

<c:if test="${!empty formBean.suppliers}">
    <z:attributeListItem label="Suppliers">
        <ul class="comma-separated">
            <c:forEach var="supplier" items="${formBean.suppliers}">
                <li>${supplier.linkWithAttributionAndOrderThis}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>
</c:if>

<c:if test="${clone.rnaClone && !empty clone.rating}">
    <z:attributeListItem label="Quality">
        <img src="/images/${clone.rating+1}0stars.gif" alt="Rating ${clone.rating +1}">
        (
        <c:choose>
            <c:when test="${clone.rating eq 0}">Probe is difficult to use. Generally basal level of expression with more intense labeling in particular structure. </c:when>
            <c:when test="${clone.rating eq 1}">Weak expression pattern</c:when>
            <c:when test="${clone.rating eq 2}">Moderate expression pattern</c:when>
            <c:when test="${clone.rating eq 3}">Nice strong expression pattern</c:when>
            <c:when test="${clone.rating eq 4}">Simple to use, intense expression pattern restricted to a few structures</c:when>
        </c:choose>
        )
    </z:attributeListItem>
</c:if>

<c:if test="${formBean.thisseProbe}">
    <z:attributeListItem label="Protocol">
        <a href="/ZFIN/Methods/ThisseProtocol.html"><b>Thisse <i>in situ </i> hybridization protocol</b></a>
    </z:attributeListItem>
</c:if>

<z:attributeListItem label="Note">
    <zfin2:entityNotes entity="${formBean.clone}"/>
    <c:if test="${!empty formBean.dbSnps}">
        Candidate SNPs in the zebrafish genome were mapped by <a href="/ZDB-PUB-070427-10">Bradley KM et al.</a> The list of reference SNPs mapped on this genomic clone has been retrieved through data exchange between NCBI and ZFIN. These reference SNP identifiers were created by NCBI during periodic 'builds' of the dbSNP database. NCBI has phased out support for non-human organisms in dbSNP and dbVar. Zebrafish SNP details can be obtained from the archive directory <a href="ftp://ftp.ncbi.nih.gov/snp/archive">ftp://ftp.ncbi.nih.gov/snp/archive</a>.<p>${formBean.dbSnps}</p>

    </c:if>
</z:attributeListItem>
