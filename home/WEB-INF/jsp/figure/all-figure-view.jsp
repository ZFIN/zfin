
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<meta name="all-figure-view-page"/> <%-- this is used by the web testing framework to know which page it is --%>

<zfin-figure:publicationInfo publication="${publication}" submitters="${submitters}" showThisseInSituLink="${showThisseInSituLink}"/>

<c:if test="${!empty probe}">
    <table class="primary-entity-attributes">
        <tr>
            <th>Probe:</th>
            <td>
                <zfin:link entity="${probe}"/>
            </td>
        </tr>
            <%-- this is deviating from the old figureview, because the nice java tag we have doesn't seem to follow
         that format.  Maybe people will like this better? --%>
        <c:if test="${!empty probeSuppliers}">
            <tr>
                <th>Supplier:</th>
                <td><c:forEach var="supplier" items="${probeSuppliers}">
                    ${supplier.linkWithAttributionAndOrderThis}
                </c:forEach></td>
            </tr>
        </c:if>
    </table>
</c:if>

<c:forEach var="figure" items="${figures}">
    <zfin-figure:imagesAndCaption figure="${figure}" autoplayVideo="false">

        <zfin-figure:expressionSummary genes="${expressionGeneMap[figure]}"
                                       antibodies="${expressionAntibodyMap[figure]}"
                                       fishesAndGenotypes="${expressionFishesAndGenotypeMap[figure]}"
                                       strs="${expressionSTRMap[figure]}"
                                       experiments="${expressionConditionMap[figure]}"
                                       entities="${expressionEntityMap[figure]}"
                                       start="${expressionStartStageMap[figure]}" end="${expressionEndStageMap[figure]}"/>
        <c:if test="${!empty expressionStartStageMap[figure]}">
            <div style="margin-top: 1em;">
                <a href="/${figure.zdbID}#expDetail">Expression / Labeling details</a>
            </div>
        </c:if>

        <zfin-figure:phenotypeSummary fishesAndGenotypes="${phenotypeFishesAndGenotypeMap[figure]}"
                                      strs="${phenotypeSTRMap[figure]}"
                                      entities="${phenotypeEntitiesMap[figure]}"
                                      experiments="${phenotypeConditionMap[figure]}"
                                      start="${phenotypeStartStageMap[figure]}" end="${phenotypeEndStageMap[figure]}" />

        <c:if test="${!empty phenotypeFishesAndGenotypeMap[figure]}">
            <div style="margin-top: 1em;">
                <a href="/${figure.zdbID}#phenoDetail">Phenotype details</a>
            </div>
        </c:if>
        <zfin-figure:constructLinks figure="${figure}"/>

    </zfin-figure:imagesAndCaption>
</c:forEach>

<c:choose>
    <c:when test="${publication.canShowImages && publication.type != 'Unpublished'}">
        <zfin2:acknowledgment publication="${publication}" showElsevierMessage="${showElsevierMessage}" hasAcknowledgment="${hasAcknowledgment}"/>
    </c:when>
    <c:otherwise>
        <zfin2:subsection>
            <zfin-figure:journalAbbrev publication="${publication}"/>
        </zfin2:subsection>
    </c:otherwise>
</c:choose>

<script>
    jQuery(document).ready(function() {
        jQuery('.fish-label').tipsy({gravity:'sw', opacity:1, delayIn:750, delayOut:200});
    });
</script>