<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>


<z:attributeList>
    <z:attributeListItem label="ID" copyable="true">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
        <a class="small" href="/action/nomenclature/history/${formBean.marker.zdbID}">Nomenclature History</a>
    </z:attributeListItem>

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}" />

    <z:attributeListItem>
        <jsp:attribute name="label">
            Transcript Type <a class="popup-link info-popup-link" href="/action/marker/transcript-types"></a>
        </jsp:attribute>
        <jsp:body>
            ${formBean.transcript.transcriptType.display}
        </jsp:body>
    </z:attributeListItem>

    <z:attributeListItem>
         <jsp:attribute name="label">
             Annotation Status <a class="popup-link info-popup-link" href="/action/marker/transcript-statuses"></a>
        </jsp:attribute>
        <jsp:body>
             ${formBean.transcript.status.display}
        </jsp:body>
    </z:attributeListItem>

    <z:attributeListItem>
         <jsp:attribute name="label">
             Annotation Method
        </jsp:attribute>
        <jsp:body>
             ${formBean.transcript.annotationMethod.name}
        </jsp:body>
    </z:attributeListItem>

    <z:attributeListItem label="Associated With Genes">
        <zfin2:toggledLinkList collection="${formBean.relatedGenes}" maxNumber="6" showAttributionLinks="true"/>
    </z:attributeListItem>

    <z:attributeListItem label="Strain">
        <zfin:link entity="${formBean.strain}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Non Reference Strain">
        <zfin2:toggledLinkList collection="${formBean.nonReferenceStrains}" maxNumber="6"/>
    </z:attributeListItem>

    <zfin2:markerGenomeResourcesAttributeListItem links="${formBean.otherMarkerPages}" />

    <zfin2:rnaCentral links="${formBean.rnaCentralLink}" />


    <zfin2:entityNotesAttributeListItems entity="${formBean.transcript}" />

</z:attributeList>



