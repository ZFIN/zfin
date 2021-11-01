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

    <c:if test="${not empty formBean.marker.fluorescentMarkers}">
        <z:attributeListItem label="[Em &lambda;][Ex &lambda;]">
            <c:if test="${formBean.marker.fluorescentMarkers != null}">
                <c:forEach var="fp" items="${formBean.marker.fluorescentMarkers}" varStatus="loop">
                    <button type="button" class="btn btn-primary"
                            style="background: ${fp.emissionColorHexFixed}; color: ${fp.textEmissionColorHexFixed}; width: 110px !important;">${fp.emissionLength}
                        (${fp.emissionColor})
                    </button>
                    <button type="button" class="btn btn-primary"
                            style="background: ${fp.excitationColorHexFixed}; color: ${fp.textExcitationColorHexFixed}; width: 110px !important;">${fp.excitationLength}
                        (${fp.excitationColor})
                    </button>
                    <a href='https://www.fpbase.org/protein/${fn:replace((fn:toLowerCase(fp.protein.name)),".", "")}'>Fpbase:${fp.protein.name}</a>
                    <c:if test="${!loop.last}"></p></c:if>
                </c:forEach>
            </c:if>
        </z:attributeListItem>
    </c:if>

    <zfin2:markerSOTypeAttributeListItem soTerm="${formBean.zfinSoTerm}"/>

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}"/>

</z:attributeList>