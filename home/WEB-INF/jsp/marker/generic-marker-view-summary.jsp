<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <zfin2:markerSOTypeAttributeListItem soTerm="${formBean.zfinSoTerm}" />

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}" />

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}" />

</z:attributeList>

