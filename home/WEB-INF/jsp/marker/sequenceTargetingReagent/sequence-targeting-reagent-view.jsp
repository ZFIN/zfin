<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="TARGETLOCATION" value="Target Location"/>
<c:set var="GENOMICFEATURES" value="Genomic Features"/>
<c:set var="CONSTRUCTS" value="Constructs"/>
<c:set var="EXPRESSION" value="Expression"/>
<c:set var="PHENOTYPE" value="Phenotype"/>

<z:dataPage
        sections="${[SUMMARY, TARGETLOCATION, CONSTRUCTS, GENOMICFEATURES, EXPRESSION, PHENOTYPE]}">

    <z:dataManagerDropdown>
        <a class="dropdown-item active" href="/action/marker/sequenceTargetingReagent/prototype-view/${formBean.marker.zdbID}">View</a>
        <a class="dropdown-item" href="/action/marker/gene/edit/${formBean.marker.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}">Merge</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/${formBean.marker.zdbID}">Old View</a>
    </z:dataManagerDropdown>


    <c:set var="suppressAnalysisTools" value="${false}"/>

    <c:set var="typeName">${formBean.marker.markerType.name}</c:set>

    <c:if test="${fn:length(formBean.markerRelationshipPresentationList) gt 1}">
        <c:set var="targetsLabel" value="Targets"/>
    </c:if>
    <c:if test="${(fn:length(formBean.markerRelationshipPresentationList)) eq 1}">
        <c:set var="targetsLabel" value="Target"/>
    </c:if>

    <div id="${zfn:makeDomIdentifier(SUMMARY)}">
        <div class="small text-uppercase text-muted">${formBean.marker.markerType.displayName}</div>
        <h1><zfin:abbrev entity="${formBean.marker}"/></h1>

        <z:attributeList>
            <z:attributeListItem label="ID">
                ${formBean.marker.zdbID}
            </z:attributeListItem>

            <z:attributeListItem label="Name">
                ${formBean.marker.name}
            </z:attributeListItem>

            <z:attributeListItem label="Synonyms">
                <ul class="comma-separated">
                    <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                        <li>${markerAlias.linkWithAttribution}</li>
                    </c:forEach>
                </ul>
            </z:attributeListItem>

            <z:attributeListItem label="${targetsLabel}">

                    <c:forEach var="entry" items="${formBean.markerRelationshipPresentationList}" varStatus="loop">
                        <i>${entry.link}</i> ${entry.attributionLink}${!loop.last ? ", " : ""}
                    </c:forEach>

            </z:attributeListItem>

            <c:if test="${formBean.marker.markerType.name ne 'MRPHLNO'}">
                <z:attributeListItem label="Source">
                    <zfin2:orderThis markerSuppliers="${formBean.suppliers}" accessionNumber="${formBean.marker.zdbID}"/>
                </z:attributeListItem>
            </c:if>


            <c:if test="${formBean.marker.markerType.name eq 'MRPHLNO'}">
                <c:set var="seqTypeName">Target Sequence</c:set>
            </c:if>
            <c:if test="${typeName eq 'TALEN'}">
                <c:set var="seqTypeName">Target Sequence 1</c:set>
            </c:if>

            <z:attributeListItem label="${seqTypeName}">
            <c:choose>
                <c:when test="${!empty formBean.marker.sequence}">
                        5' - ${formBean.marker.sequence.sequence} - 3'
                        <c:if test="${!empty formBean.sequenceAttribution}">
                            (${formBean.sequenceAttribution})
                        </c:if>
                    <c:if test="${!suppressAnalysisTools}">
                        &nbsp;&nbsp;&nbsp;
                        <c:if test="${typeName eq 'TALEN'}">
                            <c:set var="firstSeqLen">${fn:length(formBean.marker.sequence.sequence)}</c:set>
                            <c:set var="secondSeqLen">${fn:length(formBean.marker.sequence.secondSequence)}</c:set>
                        </c:if>
                        <zfin2:markerSequenceBlastDropDown
                                sequence="${formBean.marker.sequence.sequence}"
                                databases="${formBean.databases}"
                                instructions="Select Sequence Analysis Tool"
                        />
                    </c:if>

                </c:when>
                <c:otherwise>
                    <zfin2:noDataAvailable/>
                </c:otherwise>
            </c:choose>
            </z:attributeListItem>

            <c:if test="${typeName eq 'TALEN'}">
                <z:attributeListItem label="Target Sequence 2">
                    <c:choose>
                        <c:when test="${!empty formBean.marker.sequence}">
                            5' - ${formBean.marker.sequence.secondSequence} - 3'
                            <c:if test="${!empty formBean.sequenceAttribution}">
                                (${formBean.sequenceAttribution})
                            </c:if>
                            <c:if test="${!suppressAnalysisTools}">
                                &nbsp;&nbsp;&nbsp;
                                <zfin2:markerSequenceBlastDropDown
                                        sequence="${formBean.marker.sequence.secondSequence}"
                                        databases="${formBean.databases}"
                                        instructions="Select Sequence Analysis Tool"
                                />
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <zfin2:noDataAvailable/>
                        </c:otherwise>
                    </c:choose>
                </z:attributeListItem>
            </c:if>
            <z:attributeListItem label="Disclaimer">
                <c:if test="${!empty formBean.marker.sequence}">
                        Although ZFIN verifies reagent sequence data, we recommend that you
                        conduct independent sequence analysis before ordering any reagent.
                </c:if>
            </z:attributeListItem>

        </z:attributeList>

        <zfin2:entityNotes entity="${marker}"/>

    </div>

    <z:section title="${TARGETLOCATION}">
        <jsp:include page="sequence-targeting-reagent-target-location-view.jsp"/>
    </z:section>


    <z:section title="${CONSTRUCTS}">
        <jsp:include page="sequence-targeting-reagent-constructs-view.jsp"/>
    </z:section>


    <z:section title="${GENOMICFEATURES}">
        <jsp:include page="sequence-targeting-reagent-genonomicfeatures-view.jsp"/>
    </z:section>

    <z:section title="${EXPRESSION}">
        <jsp:include page="sequence-targeting-reagent-expression-view.jsp"/>
    </z:section>

    <z:section title="${PHENOTYPE}">
        <z:section title="Phenotype resulting from ${formBean.marker.name}">
            <jsp:include page="sequence-targeting-reagent-phenotype-view.jsp" />
        </z:section>
        <z:section title="Phenotype of all Fish created by or utilizing ${formBean.marker.name}">
            <jsp:include page="sequence-targeting-reagent-fish-phenotype-view.jsp" />
        </z:section>
    </z:section>
</z:dataPage>


<script src="${zfn:getAssetPath("react.js")}"></script>