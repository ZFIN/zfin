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

    <z:attributeListItem label="Variant Allele">
        ${formBean.variant}
    </z:attributeListItem>

    <z:attributeListItem label="Sequence">
        <div>${formBean.sequence.startToOffset}</div>
        <span style="color: green;">
                ${formBean.sequence.ambiguity}
        </span>
        <div>${formBean.sequence.offsetToEnd}</div>
        <div>${formBean.sequence.ambiguity} = ${formBean.variant}</div>
        <div class='btn-group'>
            <button
                    class='btn btn-outline-secondary btn-sm dropdown-toggle'
                    data-toggle='dropdown'
                    aria-haspopup='true'
                    aria-expanded='false'
            >
                Select Tool
            </button>
            <div class='dropdown-menu'>
                <a class='dropdown-item' href="${formBean.ncbiBlastUrl}${formBean.sequence.sequence}">
                    NCBI BLAST
                </a>
                <a class='dropdown-item' href="/action/blast/blast?&program=blastn&sequenceType=nt&queryType=FASTA&shortAndNearlyExact=true&expectValue=1e-10&dataLibraryString=RNASequences&querySequence=${formBean.sequence.sequence}">
                    ZFIN BLAST
                </a>
            </div>
        </div>
    </z:attributeListItem>

    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>

    <zfin2:markerGenomeResourcesAttributeListItem links="${formBean.otherMarkerPages}" />

    <zfin2:entityNotesAttributeListItems entity="${formBean.marker}" />

</z:attributeList>
