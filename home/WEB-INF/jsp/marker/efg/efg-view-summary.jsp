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

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}"/>

    <z:attributeListItem label="Emission Wavelength">
        <c:if test="${formBean.marker.fluorescentMarkers != null}">
            <c:forEach var="fp" items="${formBean.marker.fluorescentMarkers}">
                ${fp.emissionLength} nm <div id="rectangle" style="background: ${fp.emissionColorHex}; width: 30px; height: 20px; display: inline-block;"></div>
            </c:forEach>
        </c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Excitation Wavelength">
        <c:if test="${formBean.marker.fluorescentMarkers != null}">
            <c:forEach var="fp" items="${formBean.marker.fluorescentMarkers}">
                ${fp.excitationLength} nm <div id="rectangle" style="background: ${fp.excitationColorHex}; width: 30px; height: 20px; display: inline-block;"></div>
            </c:forEach>
        </c:if>
    </z:attributeListItem>

    <z:attributeListItem label="FPbase Ref" link="/action/fluorescence/proteins">
        <c:if test="${formBean.marker.fluorescentProteins != null}">
            <c:forEach var="fp" items="${formBean.marker.fluorescentProteins}">
                <a href='https://www.fpbase.org/protein/${fn:replace((fn:toLowerCase(fp.name)),".", "")}'>${fp.name}</a>
            </c:forEach>
        </c:if>
    </z:attributeListItem>

    <zfin2:markerSOTypeAttributeListItem soTerm="${formBean.zfinSoTerm}"/>

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}"/>

</z:attributeList>