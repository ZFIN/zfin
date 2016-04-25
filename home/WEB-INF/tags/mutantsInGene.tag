<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relatissonships in a table --%>

<%@ attribute name="mutantsOnMarkerBean" required="true" rtexprvalue="true"
              type="org.zfin.marker.presentation.MutantOnMarkerBean" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>

<%@ attribute name="title" required="false" %>


<c:if test="${empty title}">
    <c:set var="title" value="MUTATIONS AND SEQUENCE TARGETING REAGENTS"/>
</c:if>

<zfin2:subsection title="${title}"
                  test="${!empty mutantsOnMarkerBean and (!empty mutantsOnMarkerBean.features or !empty mutantsOnMarkerBean.knockdownReagents)}"
                  showNoData="true">



    <c:if test="${!empty mutantsOnMarkerBean.features}">

        <div>
            <table class="summary rowstripes alleles">
                <tr>
                    <th width="10%">Allele</th>
                    <th width="13%">Type</th>
                    <th width="15%">Localization</th>
                    <th width="20%">Consequence</th>
                    <th width="10%">Mutagen</th>
                    <th width="50%">Suppliers</th>
                </tr>
                <c:forEach var="feature" items="${mutantsOnMarkerBean.features}" varStatus="loop">
                    <tr class=${loop.index%2==0 ? "even" : "odd"}>
                        <td>
                            <a href="/${feature.zdbID}">${feature.abbreviation}</a>
                        </td>
                        <td>
                            ${feature.type.display}
                        </td>
                        <td>
                                ${feature.geneLocalizationStmt}
                        </td>
                        <td>
                            <c:forEach var="consequence" items="${feature.featureTranscriptMutationDetailSet}" varStatus="loop">

                                    ${consequence.transcriptConsequence.displayName}
                                <c:if test="${!loop.last}">,&nbsp;</c:if>
                            </c:forEach>
                        </td>

                        <td>
                            <c:set var="mutagen" value="${feature.featureAssay.mutagen}"/>
                            <c:if test="${mutagen ne zfn:getMutagen('not specified')}">
                                ${feature.featureAssay.mutagen}
                            </c:if>
                        </td>
                        <td>
                            <c:forEach var="supplier" items="${feature.suppliers}">
                                <li style="list-style-type: none;">
                                    <a href="/${supplier.organization.zdbID}"> ${supplier.organization.name}</a>
                                    <c:if test="${!empty supplier.orderURL}">
                                        <a href="${supplier.orderURL}"> (order this)</a>
                                    </c:if>
                                </li>
                            </c:forEach>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </c:if>

    <table class="summary horizontal-solidblock">


        <c:if test="${!empty mutantsOnMarkerBean.knockdownReagents}">
            <tr>
                <td class="data-label"><b>Targeting reagents:</b> </td>
                <td>
                    <zfin2:toggledProvidesLinkList collection="${mutantsOnMarkerBean.knockdownReagents}" maxNumber="5"/>
                </td>
            </tr>
        </c:if>
    </table>


</zfin2:subsection>

