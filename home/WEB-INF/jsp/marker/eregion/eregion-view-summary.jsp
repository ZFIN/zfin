<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        ${formBean.marker.name}
    </z:attributeListItem>

    <z:attributeListItem label="Symbol">
        ${formBean.marker.abbreviation}
    </z:attributeListItem>

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}" />
    
  
    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}" />

</z:attributeList>