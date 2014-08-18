<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishBean" scope="request"/>

<div class="popup-header">
    Fish Name: ${formBean.fish.name}
</div>
<div class="popup-body">
    <table class="primary-entity-attributes">
        <tr>
            <th class="genotype-name-label">
                <c:if test="${!formBean.genotype.wildtype}">
                    <span class="name-label">Genotype:</span>
                </c:if>
                <c:if test="${formBean.genotype.wildtype}">
                    <span class="name-value">Wild-Type Line:</span>
                </c:if>
            </th>
            <td class="genotype-name-value">
                <span class="name-value"><zfin:link entity="${formBean.genotype}"/></span>
            </td>
        </tr>


        <c:if test="${formBean.genotype.wildtype}">
            <tr>
                <th>
                    <span class="name-label">Abbreviation:</span>
                </th>
                <td>
                    <span class="name-value">${formBean.genotype.handle}</span>
                </td>
            </tr>
        </c:if>



        <c:if test="${!formBean.genotype.wildtype}">
            <tr>
                <th>
                    Background:
                </th>
                <td>
                    <c:choose>
                        <c:when test="${fn:length(formBean.genotype.associatedGenotypes) ne null && fn:length(formBean.genotype.associatedGenotypes) > 0}">
                            <c:forEach var="background" items="${formBean.genotype.associatedGenotypes}" varStatus="loop">
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

    <c:if test="${!empty formBean.genomicFeatures}">
        <zfin2:subsection title="GENOTYPE COMPOSITION">
            <table class="summary rowstripes">
                <tr>
                    <th width="25%">Genomic Feature</th>
                    <th>Affected Gene</th>
                    <th>Construct</th>
                </tr>
                <c:forEach var="genomicFeature" items="${formBean.genomicFeatures}" varStatus="gfLoop">
                    <zfin:alternating-tr loopName="gfLoop">
                        <td><zfin:link entity="${genomicFeature.feature}"/>
                        <td><zfin:link entity="${genomicFeature.gene}"/>
                        <td><zfin:link entity="${genomicFeature.construct}"/>
                    </zfin:alternating-tr>
                </c:forEach>
            </table>
        </zfin2:subsection>
    </c:if>
    
    <c:if test="${!empty formBean.sequenceTargetingReagents}">
        <zfin2:subsection title="SEQUENCE TARGETING REAGENTS">
        <table class="summary rowstripes">
            <tr>
                <th width="20%">Reagent</th>
                <th>Affected Gene</th>
            </tr>
            <c:forEach var = "reagent" items="${formBean.sequenceTargetingReagents}" varStatus="rLoop">
                <zfin:alternating-tr loopName="rLoop">
                    <td><zfin:link entity="${reagent}"/></td>
                    <td><zfin2:toggledHyperlinkList collection="${reagent.targetGenes}" maxNumber="5"/></td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
        </zfin2:subsection>
    </c:if>
    
</div>