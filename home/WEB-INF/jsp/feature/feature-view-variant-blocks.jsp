<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<%-- A Complex feature (ZFIN-10622): one block per variant, each with everything a single-variant
     feature shows in its Variants section (see feature-view-variants.jsp and
     feature-view-mut-details.jsp). --%>
<c:set var="hideLocationLink" value="${empty formBean.feature.affectedGenes || isDeficiency}"/>

<c:forEach var="variant" items="${formBean.variantDisplays}">
    <z:section title="Variant ${variant.variantNumber}" sectionID="variant-${variant.variantNumber}">

        <z:attributeList>
            <z:attributeListItem label="Variant Type">
                ${variant.variantType}
            </z:attributeListItem>

            <z:attributeListItem label="Variant Location">
                <c:set var="location" value="${variant.location}"/>
                <c:if test="${!empty location}">
                    Chr ${location.chromosome}: ${location.startLocation}
                    <c:if test="${!empty location.endLocation && location.startLocation != location.endLocation}">
                        - ${location.endLocation}
                    </c:if>
                    <c:if test="${!empty location.assembly}">
                        (${location.assembly})
                    </c:if>
                    <c:forEach var="reference" items="${location.references}" end="0">
                        (<a href="/${reference.sourceZdbID}">1</a>)
                    </c:forEach>
                    <c:if test="${!hideLocationLink}">
                        <a href="/action/mapping/detail/${formBean.feature.zdbID}#physicalMapping">Details</a>
                    </c:if>
                </c:if>
            </z:attributeListItem>

            <z:attributeListItem label="Nucleotide Change">
                <span style="word-wrap: break-word;">${variant.nucleotideChange}</span>
            </z:attributeListItem>

            <z:attributeListItem label="Variant Notes">
                <z:ifHasData test="${!empty variant.variantNotes}" noDataMessage="None">
                    <c:forEach var="note" items="${variant.variantNotes}" varStatus="loop">
                        <div class="${loop.last ? '' : 'mb-2'}">
                            <zfin2:toggleTextLength
                                    text="${note.note}"
                                    idName="${zfn:generateRandomDomID()}"
                                    shortLength="80"
                                    section="variants"
                            />
                            <div>
                                <zfin:link entity="${note.publication}"/>
                            </div>
                        </div>
                    </c:forEach>
                </z:ifHasData>
            </z:attributeListItem>
        </z:attributeList>

        <z:section title="Effect on DNA/cDNA, transcript, protein (from publications)"
                   sectionID="variant-${variant.variantNumber}-effects">
            <zfin2:mutationDetailsAttributeList feature="${formBean.feature}"
                                                mutationDetails="${variant.mutationDetails}"
                                                dnaChangeAttributions="${variant.dnaChangeAttributions}"
                                                transcriptConsequenceAttributions="${variant.transcriptConsequenceAttributions}"
                                                proteinConsequenceAttributions="${variant.proteinConsequenceAttributions}"
                                                variantNumber="${variant.variantNumber}"/>
        </z:section>

    </z:section>
</c:forEach>

<%-- Variant notes curated on the feature rather than on one of its variants. --%>
<c:if test="${!empty formBean.variantNotes}">
    <z:attributeList>
        <z:attributeListItem label="Variant Notes">
            <c:forEach var="note" items="${formBean.variantNotes}" varStatus="loop">
                <div class="${loop.last ? '' : 'mb-2'}">
                    <zfin2:toggleTextLength
                            text="${note.note}"
                            idName="${zfn:generateRandomDomID()}"
                            shortLength="80"
                            section="variants"
                    />
                    <div>
                        <zfin:link entity="${note.publication}"/>
                    </div>
                </div>
            </c:forEach>
        </z:attributeListItem>
    </z:attributeList>
</c:if>
