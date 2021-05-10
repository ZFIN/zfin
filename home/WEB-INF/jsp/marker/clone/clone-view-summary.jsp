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
    </z:attributeListItem>

    <zfin2:markerPreviousNamesAttributeListItem previousNames="${formBean.previousNames}" />

    <zfin2:markerSOTypeAttributeListItem soTerm="${formBean.zfinSoTerm}" />

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <zfin2:markerGenomeResourcesAttributeListItem links="${formBean.otherMarkerPages}" />

    <c:if test="${!empty formBean.clone.problem}">
        <z:attributeListItem label="Clone Problem Type">
            ${formBean.clone.problem} <i class="warning-icon"></i>
        </z:attributeListItem>
    </c:if>

    <z:attributeListItem label="Species">
        ${formBean.clone.probeLibrary.species}
    </z:attributeListItem>

    <z:attributeListItem label="Library">
        ${formBean.clone.probeLibrary.name}
    </z:attributeListItem>

    <%--<zfin2:cloneData clone="${formBean.clone}" isThisseProbe="${formBean.isThisseProbe}"/>--%>
    <zfin2:cloneSummary formBean="${formBean}"/>

</z:attributeList>

<zfin2:uninformativeCloneName name="${formBean.marker.abbreviation}" chimericClone="${formBean.marker.chimeric}"/>