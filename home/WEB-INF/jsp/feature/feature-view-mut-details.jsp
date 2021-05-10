<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<z:attributeList>

    <z:attributeListItem label="DNA/cDNA Change">
        <z:ifHasData test="${!empty formBean.mutationDetails.dnaChangeStatement}" noDataMessage="None">

            ${formBean.mutationDetails.dnaChangeStatement}
            <c:choose>
                <c:when test="${fn:length(formBean.dnaChangeAttributions) == 1}">
                    (<a href="/${formBean.dnaChangeAttributions[0].sourceZdbID}">1</a>)
                </c:when>
                <c:when test="${fn:length(formBean.dnaChangeAttributions) > 1}">
                    (<a href="/action/feature/${formBean.feature.zdbID}/mutation-detail-citations?type=dna">${fn:length(formBean.dnaChangeAttributions)}</a>)
                </c:when>
            </c:choose>
        </z:ifHasData>
    </z:attributeListItem>
    <z:attributeListItem label="Transcript Consequence">
        <z:ifHasData test="${!empty formBean.mutationDetails.transcriptChangeStatement}" noDataMessage="None">

            ${formBean.mutationDetails.transcriptChangeStatement}
            <c:choose>
                <c:when test="${fn:length(formBean.transcriptConsequenceAttributions) == 1}">
                    (<a href="/${formBean.transcriptConsequenceAttributions[0].sourceZdbID}">1</a>)
                </c:when>
                <c:when test="${fn:length(formBean.transcriptConsequenceAttributions) > 1}">
                    (<a href="/action/feature/${formBean.feature.zdbID}/mutation-detail-citations?type=transcript">${fn:length(formBean.transcriptConsequenceAttributions)}</a>)
                </c:when>
            </c:choose>

        </z:ifHasData>
    </z:attributeListItem>
    <z:attributeListItem label="Protein Consequence">
        <z:ifHasData test="${!empty formBean.mutationDetails.proteinChangeStatement}" noDataMessage="None">

            ${formBean.mutationDetails.proteinChangeStatement}
            <c:choose>
                <c:when test="${fn:length(formBean.proteinConsequenceAttributions) == 1}">
                    (<a href="/${formBean.proteinConsequenceAttributions[0].sourceZdbID}">1</a>)
                </c:when>
                <c:when test="${fn:length(formBean.proteinConsequenceAttributions) > 1}">
                    (<a href="/action/feature/${formBean.feature.zdbID}/mutation-detail-citations?type=protein">${fn:length(formBean.proteinConsequenceAttributions)}</a>)
                </c:when>
            </c:choose>


        </z:ifHasData>
    </z:attributeListItem>
</z:attributeList>

