<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<c:set var="location" value="${formBean.featureLocations[0]}"/>
<z:attributeList>
    <z:attributeListItem label="Variant Type">
        ${formBean.feature.type.display}
    </z:attributeListItem>

    <z:attributeListItem label="Variant Location">

        <c:choose>
            <c:when test="${fn:length(formBean.featureLocations)>0}">

                <zfin2:displayFullLocation location="${formBean.featureLocations[0]}"
                                           hideLink="${empty formBean.feature.affectedGenes}"/>

            </c:when>
            <c:otherwise>
                <zfin2:displayLocation entity="${formBean.feature}"
                                       hideLink="${empty formBean.feature.affectedGenes}"/>
            </c:otherwise>
        </c:choose>

    </z:attributeListItem>

    <z:attributeListItem label="Nucleotide change">
        ${formBean.varSequence.vfsVariation}
    </z:attributeListItem>

    <z:attributeListItem label="Variant Notes">

        <z:ifHasData test="${!empty formBean.externalNotes and fn:length(formBean.externalNotes) > 0}"
                     noDataMessage="None">

                <c:forEach var="note" items="${formBean.externalNotes}" varStatus="loop">

                    <c:if test="${note.tag.contains('variant')}">
                        <div class="${loop.last ? '' : 'mb-2'}">
                        <zfin2:toggleTextLength
                                text="${note.note}"
                                idName="${zfn:generateRandomDomID()}"
                                shortLength="80"
                        />
                         <div>
                              <zfin:link entity="${note.publication}"/>
                         </div>
                        </div>

                    </c:if>

                </c:forEach>

        </z:ifHasData>

    </z:attributeListItem>


</z:attributeList>