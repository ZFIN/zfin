<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>
<c:set var="genotype" value="${formBean.genotype}"/>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${genotype.zdbID}
    </z:attributeListItem>

    <c:if test="${!formBean.genotype.wildtype}">
        <z:attributeListItem label="Name">
            <zfin:name entity="${genotype}"/>
        </z:attributeListItem>

        <z:attributeListItem label="Previous Name">
            <zfin2:previousNamesFastNew label="Previous Name" previousNames="${formBean.previousNames}"/>
        </z:attributeListItem>

        <z:attributeListItem label="Background">
            <c:choose>
                <c:when test="${fn:length(genotype.associatedGenotypes) ne null && fn:length(genotype.associatedGenotypes) > 0}">
                    <c:forEach var="background" items="${genotype.associatedGenotypes}" varStatus="loop">
                        <zfin:link entity="${background}"/>
                        <c:if test="${background.handle != background.name}">(${background.handle})</c:if>
                        <c:if test="${!loop.last}">,&nbsp;</c:if>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    Unspecified
                </c:otherwise>
            </c:choose>
        </z:attributeListItem>

        <z:attributeListItem label="Affected Genomic Region">
            <c:forEach var="affectedGene" items="${affectedMarkerList}" varStatus="loop">
                <zfin:link entity="${affectedGene}"/><c:if test="${!loop.last}">,&nbsp;</c:if>
            </c:forEach>
        </z:attributeListItem>
    </c:if>
    <%-- If wild type show special header--%>
    <c:if test="${genotype.wildtype}">
        <z:attributeListItem label="Wild-Type Line">
            ${genotype.name}
        </z:attributeListItem>
        <z:attributeListItem label="Abbrevitation">
            ${genotype.handle}
        </z:attributeListItem>
        <z:attributeListItem label="Previous Name">
            <zfin2:previousNamesFastNew label="Previous Name" previousNames="${formBean.previousNames}"/>
        </z:attributeListItem>
    </c:if>
    <z:attributeListItem label="Current Source">
        <zfin2:genotypeSummarySuppliers genotype="${genotype}"/>
    </z:attributeListItem>
    <c:if test="${genotype.wildtype}">
        <z:attributeListItem label="Wild-Type Lines">
            <a href="/action/feature/wildtype-list">Show All</a>
        </z:attributeListItem>
    </c:if>

</z:attributeList>