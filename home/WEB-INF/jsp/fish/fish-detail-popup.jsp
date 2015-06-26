<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    Fish Name: ${fish.name}
</div>
<div class="popup-body">
    <table class="primary-entity-attributes">
        <tr>
            <th class="genotype-name-label">
                <c:if test="${!fish.genotype.wildtype}">
                    <span class="name-label">Genotype:</span>
                </c:if>
                <c:if test="${fish.genotype.wildtype}">
                    <span class="name-value">Wild-Type Line:</span>
                </c:if>
            </th>
            <td class="genotype-name-value">
                <span class="name-value"><zfin:link entity="${fish.genotype}"/></span>
            </td>
        </tr>


        <c:if test="${fish.genotype.wildtype}">
            <tr>
                <th>
                    <span class="name-label">Abbreviation:</span>
                </th>
                <td>
                    <span class="name-value">${fish.genotype.handle}</span>
                </td>
            </tr>
        </c:if>



        <c:if test="${!fish.genotype.wildtype}">
            <tr>
                <th>
                    Background:
                </th>
                <td>
                    <c:choose>
                        <c:when test="${fn:length(fish.genotype.associatedGenotypes) ne null && fn:length(fish.genotype.associatedGenotypes) > 0}">
                            <c:forEach var="background" items="${fish.genotype.associatedGenotypes}" varStatus="loop">
                                <zfin:link entity="${background}"/>
                                <c:if test="${background.handle != background.name}">(${background.handle})</c:if>
                                <c:if test="${!loop.last}">,&nbsp;</c:if>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            Unspecified
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty fishGenomicFeatures}">
        <zfin2:subsection title="GENOTYPE COMPOSITION">
            <table class="summary rowstripes">
                <tr>
                    <th width="25%">Genomic Feature</th>
                    <th>Affected Gene</th>
                    <th>Construct</th>
                </tr>
                <c:forEach var="genomicFeature" items="${fishGenomicFeatures}" varStatus="gfLoop">
                    <zfin:alternating-tr loopName="gfLoop">
                        <td><zfin:link entity="${genomicFeature.feature}"/>
                        <td><zfin:link entity="${genomicFeature.gene}"/>
                        <td><zfin:link entity="${genomicFeature.construct}"/>
                    </zfin:alternating-tr>
                </c:forEach>
            </table>
        </zfin2:subsection>
    </c:if>
    
    <c:if test="${!empty fish.strList}">
        <zfin2:subsection title="SEQUENCE TARGETING REAGENTS">
        <table class="summary rowstripes">
            <tr>
                <th width="20%">Reagent</th>
                <th>Affected Gene</th>
            </tr>
            <c:forEach var = "reagent" items="${fish.strList}" varStatus="rLoop">
                <zfin:alternating-tr loopName="rLoop">
                    <td><zfin:link entity="${reagent}"/></td>
                    <td><zfin2:toggledHyperlinkList collection="${reagent.targetGenes}" maxNumber="5"/></td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
        </zfin2:subsection>
    </c:if>
    
</div>