<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Previous Names">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                <li>${markerAlias.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Type">
        <zfin2:externalLink
                href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">${formBean.zfinSoTerm.termName}</zfin2:externalLink>
    </z:attributeListItem>

    <z:attributeListItem label="Regulatory Regions">
        <ul class="comma-separated">
            <c:forEach var="regulatoryRegion" items="${formBean.regulatoryRegionPresentations}" varStatus="loop">
                <li>${regulatoryRegion.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Coding Sequences">
        <ul class="comma-separated">
            <c:forEach var="codingSequence" items="${formBean.codingSequencePresentations}" varStatus="loop">
                <li>${codingSequence.linkWithAttribution}</li>
            </c:forEach>
        </ul>

    </z:attributeListItem>

    <z:attributeListItem label="Contains Sequences">
        <ul class="comma-separated">
            <c:forEach var="containSequence" items="${formBean.containsSequencePresentations}" varStatus="loop">
                <li>${containSequence.linkWithAttribution}</li>
            </c:forEach>
        </ul>

    </z:attributeListItem>


</z:attributeList>
