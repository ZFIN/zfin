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

    <z:attributeListItem label="Previous Names">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${formBean.previousNames}">
                <li>${markerAlias.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Type">
        <zfin2:externalLink href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">
            ${formBean.zfinSoTerm.termName}
        </zfin2:externalLink>
    </z:attributeListItem>

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <z:attributeListItem label="Description">
        ${geneDesc.gdDesc}
    </z:attributeListItem>

    <z:attributeListItem label="Note">
        <zfin2:entityNotes entity="${formBean.marker}"/>
    </z:attributeListItem>
</z:attributeList>