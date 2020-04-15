<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>


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

    <z:attributeListItem label="Annotation Status">
        ${formBean.transcript.status.display}
    </z:attributeListItem>

<z:attributeListItem label="Associated With Genes">
    <zfin2:toggledLinkList collection="${formBean.relatedGenes}"
                           maxNumber="6"
                           showAttributionLinks="true"/>
</z:attributeListItem>
    <z:attributeListItem label="Strain">
        <zfin:link entity="${formBean.strain}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Non Reference Strain">
        <zfin2:toggledLinkList collection="${formBean.nonReferenceStrains}"
                               maxNumber="6"/>
    </z:attributeListItem>

    <z:attributeListItem label="Genome Resources">

        <c:forEach var="link" items="${formBean.otherMarkerPages}" varStatus="loop">
            <c:if test="${!link.displayName.contains('VEGA')}">

                <a href="${link.link}">${link.displayName}</a>
                ${link.attributionLink}<c:if test="${!loop.last}">,&nbsp;</c:if>
            </c:if>

        </c:forEach>
    </z:attributeListItem>

    <z:attributeListItem label="RNACentral">
        <c:if test="${formBean.rnaCentralLink eq 'yes'}">
            <a href=""><b>RNACentral</b></a>
        </c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Citations">
        <a href="/action/marker/citation-list/${formBean.marker.zdbID}">(${formBean.numPubs})</a>
    </z:attributeListItem>

</z:attributeList>

<c:if test="${formBean.marker.transcriptType.display eq 'miRNA'}">
    <zfin2:transcriptTargets transcriptTargets="${formBean.transcriptTargets}"/>
</c:if>

