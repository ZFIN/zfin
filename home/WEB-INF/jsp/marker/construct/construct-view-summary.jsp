<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
    </z:attributeListItem>

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}" />
    
    <zfin2:markerSOTypeAttributeListItem soTerm="${formBean.zfinSoTerm}" />

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

    <z:attributeListItem label="Contains Sequences">
        <z:ifHasData test="${!empty formBean.containsSequencePresentations}" noDataMessage="None">
            <ul class="comma-separated">
                <c:forEach var="containSequence" items="${formBean.containsSequencePresentations}">
                    <li>${containSequence.linkWithAttribution}</li>
                </c:forEach>
            </ul>
        </z:ifHasData>
    </z:attributeListItem>

    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}" />

</z:attributeList>
