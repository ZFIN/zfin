<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="suppressAnalysisTools" value="${false}"/>
<c:set var="typeName">${formBean.marker.markerType.name}</c:set>

<c:if test="${fn:length(formBean.markerRelationshipPresentationList) gt 1}">
    <c:set var="targetsLabel" value="Targets"/>
</c:if>
<c:if test="${(fn:length(formBean.markerRelationshipPresentationList)) eq 1}">
    <c:set var="targetsLabel" value="Target"/>
</c:if>

<c:if test="${typeName eq 'MRPHLNO' || typeName eq 'CRISPR'}">
    <c:set var="seqTypeName" value="Sequence"/>
</c:if>
<c:if test="${typeName eq 'TALEN'}">
    <c:set var="seqTypeName" value="Target Sequence 1"/>
</c:if>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        ${formBean.marker.name}
    </z:attributeListItem>

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}" />

    <z:attributeListItem label="${targetsLabel}">
        <ul class="comma-separated">
            <c:forEach var="entry" items="${formBean.markerRelationshipPresentationList}">
                <li><i>${entry.link}</i> ${entry.attributionLink}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="${seqTypeName}">
        <z:ifHasData test="${!empty formBean.marker.sequence}">
            <span class="mr-3">
                5' - ${formBean.marker.sequence.sequence} - 3'
                <c:if test="${!empty formBean.sequenceAttribution}">
                    (${formBean.sequenceAttribution})
                </c:if>
            </span>
            <c:if test="${!suppressAnalysisTools}">
                <zfin2:strBlastDropdown
                        sequence="${formBean.marker.sequence.sequence}"
                        databases="${formBean.databases}"/>
            </c:if>
        </z:ifHasData>
    </z:attributeListItem>

    <c:if test="${typeName eq 'TALEN'}">
        <z:attributeListItem label="Target Sequence 2">
            <z:ifHasData test="${!empty formBean.marker.sequence.secondSequence}">
                <span class="mr-3">
                    5' - ${formBean.marker.sequence.secondSequence} - 3'
                    <c:if test="${!empty formBean.sequenceAttribution}">
                        (${formBean.sequenceAttribution})
                    </c:if>
                </span>
                <c:if test="${!suppressAnalysisTools}">
                    <zfin2:strBlastDropdown
                            sequence="${formBean.marker.sequence.secondSequence}"
                            databases="${formBean.databases}"
                    />
                </c:if>
            </z:ifHasData>
        </z:attributeListItem>
    </c:if>


    <z:attributeListItem label="Disclaimer">
        <c:if test="${!empty formBean.marker.sequence}">
            Although ZFIN verifies reagent sequence data, we recommend that you
            conduct independent sequence analysis before ordering any reagent.
        </c:if>
    </z:attributeListItem>
    
    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}" />

    <zfin2:markerGenomeResourcesAttributeListItem links="${formBean.otherMarkerPages}" />

</z:attributeList>
