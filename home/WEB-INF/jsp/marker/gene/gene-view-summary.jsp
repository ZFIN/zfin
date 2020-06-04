<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Symbol">
        <zfin:abbrev entity="${formBean.marker}"/>
        <a class="small" href="/action/nomenclature/history/${formBean.marker.zdbID}">Nomenclature History</a>
    </z:attributeListItem>
    
    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}" />
    
    <zfin2:markerSOTypeAttributeListItem soTerm="${formBean.zfinSoTerm}" />

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <zfin2:geneDescriptionAttributeListItem geneDescription="${formBean.allianceGeneDesc}" />

    <zfin2:markerGenomeResourcesAttributeListItem links="${formBean.otherMarkerPages}" />

    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}" />

</z:attributeList>