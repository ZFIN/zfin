<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
    </z:attributeListItem>

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}"/>

    <zfin2:markerSOTypeAttributeListItem soTerm="${formBean.zfinSoTerm}"/>

    <z:attributeListItem label="Regulatory Regions">
        <z:ifHasData test="${!empty formBean.regulatoryRegionPresentations}" noDataMessage="None">
            <ul class="comma-separated">
                <c:forEach var="regulatoryRegion" items="${formBean.regulatoryRegionPresentations}">
                    <li>${regulatoryRegion.linkWithAttribution}</li>
                </c:forEach>
            </ul>
        </z:ifHasData>
    </z:attributeListItem>

    <z:attributeListItem label="Coding Sequences">
        <z:ifHasData test="${!empty formBean.codingSequencePresentations}" noDataMessage="None">
            <ul class="comma-separated">
                <c:forEach var="codingSequence" items="${formBean.codingSequencePresentations}">
                    <li>${codingSequence.linkWithAttribution}</li>
                </c:forEach>
            </ul>
        </z:ifHasData>
    </z:attributeListItem>

    <z:attributeListItem label="[Em &lambda;][Ex &lambda;], Other Resources">
        <c:if test="${formBean.marker.fluorescentMarkers != null}">
            <c:forEach var="fp" items="${formBean.marker.fluorescentMarkers}" varStatus="loop">
                <button type="button" class="btn btn-primary" style="background: ${fp.emissionColorHex}; width: 110px !important;" >${fp.emissionLength} (${fp.emissionColor})</button>
                <button type="button" class="btn btn-primary" style="background: ${fp.excitationColorHex};width: 110px !important;" >${fp.excitationLength} (${fp.excitationColor})</button>
                <a href='https://www.fpbase.org/protein/${fn:replace((fn:toLowerCase(fp.protein.name)),".", "")}'>Fpbase:${fp.protein.name}</a>
                <c:if test="${!loop.last}"></p></c:if>
            </c:forEach>
        </c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Contains Sequences">
        <z:ifHasData test="${!empty formBean.containsSequencePresentations}" noDataMessage="None">
            <ul class="comma-separated">
                <c:forEach var="containSequence" items="${formBean.containsSequencePresentations}">
                    <li>${containSequence.linkWithAttribution}</li>
                </c:forEach>
            </ul>
        </z:ifHasData>
    </z:attributeListItem>

    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}"/>

</z:attributeList>
