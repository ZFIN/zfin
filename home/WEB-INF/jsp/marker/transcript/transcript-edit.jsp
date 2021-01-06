<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="transcript" class="org.zfin.marker.Transcript" scope="request"/>

<c:set var="NOMENCLATURE" value="Nomenclature" />
<c:set var="ATTRIBUTES" value="Attributes" />
<c:set var="RELATIONSHIPS" value="Transcript Relationships" />
<c:set var="NOTES" value="Notes" />
<c:set var="SUPPSEQUENCES" value="Supporting Sequences" />
<c:set var="RNASEQUENCES" value="RNA Sequences" />
<c:set var="PROTSEQUENCES" value="Protein Sequences" />

<c:set var="typeName">${transcript.transcriptType.display}</c:set>

<c:if test="${typeName ne 'mRNA'}">
    <c:set var="sections" value="${[NOMENCLATURE, ATTRIBUTES, RELATIONSHIPS, NOTES, SUPPSEQUENCES, RNASEQUENCES]}"/>
</c:if>
<c:if test="${typeName eq 'mRNA'}">
    <c:set var="sections" value="${[NOMENCLATURE, ATTRIBUTES, RELATIONSHIPS, NOTES, SUPPSEQUENCES, RNASEQUENCES, PROTSEQUENCES]}"/>
</c:if>

<z:dataPage sections="${sections}">
<jsp:attribute name="entityName">
        <zfin:abbrev entity="${formBean.marker}"/>
    </jsp:attribute>
    <jsp:body>
    <z:dataManagerDropdown>
        <a class="dropdown-item" href="/${transcript.zdbID}">View</a>
        <a class="dropdown-item" href="/action/marker/marker-edit?zdbID=${transcript.zdbID}">Edit</a>
        <a class="dropdown-item" href="/action/marker/merge?zdbIDToDelete=${transcript.zdbID}">Merge</a>
        <a class="dropdown-item" href="/action/infrastructure/deleteRecord/${transcript.zdbID}">Delete</a>
        <div class="dropdown-divider"></div>
        <a class="dropdown-item" href="/action/marker/transcript/prototype-edit/${transcript.zdbID}">Prototype Edit</a>
    </z:dataManagerDropdown>

    <h1>Edit ${transcript.zdbID}</h1>

    <z:section title="${NOMENCLATURE}">
        <div class="__react-root"
             id="MarkerEditNomenclature"
             data-marker-id="${transcript.zdbID}"
             data-show-abbreviation-field="false"
             data-show-reason-fields="false">
        </div>
    </z:section>
        <z:section title="${ATTRIBUTES}">
            <div class="__react-root"
                 id="MarkerEditNomenclature"
                 data-marker-id="${transcript.zdbID}"
                 data-show-abbreviation-field="false"
                 data-show-reason-fields="false">
            </div>
        </z:section>

    <z:section title="${RELATIONSHIPS}">
        <div class="__react-root"
             id="MarkerEditMarkerRelationships"
             data-marker-abbreviation="${transcript.abbreviation}"
             data-marker-id="${transcript.zdbID}"
             data-show-relationship-type="true"
             data-relationship-type-data='${markerRelationshipTypes}'>
        </div>
    </z:section>

    <z:section title="${NOTES}">
        <div class="__react-root"
             id="MarkerEditNotes"
             data-current-user-id="${currentUser.zdbID}"
             data-marker-id="${transcript.zdbID}">
        </div>
    </z:section>

    <z:section title="${SUPPSEQUENCES}">
        <div class="__react-root" id="MarkerEditSequences" data-marker-id="${transcript.zdbID}"></div>
    </z:section>
    <z:section title="${RNASEQUENCES}">
        <div class="__react-root" id="MarkerAddSequences" data-marker-id="${transcript.zdbID}" data-type="Nucleotide"></div>
    </z:section>
    <c:if test="${typeName eq 'mRNA'}">
     <z:section title="${PROTSEQUENCES}">
        <div class="__react-root" id="MarkerAddSequences" data-marker-id="${transcript.zdbID}" data-type="Protein"></div>
     </z:section>
    </c:if>

    </jsp:body>
</z:dataPage>
