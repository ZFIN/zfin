<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- DNA/cDNA change, transcript and protein consequence statements with their citations, for a
     feature or, on a Complex feature (ZFIN-10622), for one of its variants. --%>
<%@ attribute name="feature" required="true" type="org.zfin.feature.Feature" %>
<%@ attribute name="mutationDetails" required="false" type="org.zfin.feature.presentation.MutationDetailsPresentation" %>
<%@ attribute name="dnaChangeAttributions" required="false" type="java.util.List" %>
<%@ attribute name="transcriptConsequenceAttributions" required="false" type="java.util.List" %>
<%@ attribute name="proteinConsequenceAttributions" required="false" type="java.util.List" %>
<%@ attribute name="variantNumber" required="false" type="java.lang.Integer" %>

<c:set var="citationsUrl" value="/action/feature/${feature.zdbID}/mutation-detail-citations?type="/>
<c:if test="${!empty variantNumber}">
    <c:set var="citationsUrl" value="/action/feature/${feature.zdbID}/mutation-detail-citations?variant=${variantNumber}&type="/>
</c:if>

<z:attributeList>

    <z:attributeListItem label="DNA/cDNA Change">
        <z:ifHasData test="${!empty mutationDetails.dnaChangeStatement}" noDataMessage="None">

            ${mutationDetails.dnaChangeStatement}
            <c:choose>
                <c:when test="${fn:length(dnaChangeAttributions) == 1}">
                    (<a href="/${dnaChangeAttributions[0].sourceZdbID}">1</a>)
                </c:when>
                <c:when test="${fn:length(dnaChangeAttributions) > 1}">
                    (<a href="${citationsUrl}dna">${fn:length(dnaChangeAttributions)}</a>)
                </c:when>
            </c:choose>
        </z:ifHasData>
    </z:attributeListItem>
    <z:attributeListItem label="Transcript Consequence">
        <z:ifHasData test="${!empty mutationDetails.transcriptChangeStatement}" noDataMessage="None">

            ${mutationDetails.transcriptChangeStatement}
            <c:choose>
                <c:when test="${fn:length(transcriptConsequenceAttributions) == 1}">
                    (<a href="/${transcriptConsequenceAttributions[0].sourceZdbID}">1</a>)
                </c:when>
                <c:when test="${fn:length(transcriptConsequenceAttributions) > 1}">
                    (<a href="${citationsUrl}transcript">${fn:length(transcriptConsequenceAttributions)}</a>)
                </c:when>
            </c:choose>

        </z:ifHasData>
    </z:attributeListItem>
    <z:attributeListItem label="Protein Consequence">
        <z:ifHasData test="${!empty mutationDetails.proteinChangeStatement}" noDataMessage="None">

            ${mutationDetails.proteinChangeStatement}
            <c:choose>
                <c:when test="${fn:length(proteinConsequenceAttributions) == 1}">
                    (<a href="/${proteinConsequenceAttributions[0].sourceZdbID}">1</a>)
                </c:when>
                <c:when test="${fn:length(proteinConsequenceAttributions) > 1}">
                    (<a href="${citationsUrl}protein">${fn:length(proteinConsequenceAttributions)}</a>)
                </c:when>
            </c:choose>


        </z:ifHasData>
    </z:attributeListItem>
</z:attributeList>
