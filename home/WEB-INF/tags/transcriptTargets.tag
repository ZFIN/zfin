<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="transcriptTargets" required="true"
              rtexprvalue="true" type="org.zfin.marker.presentation.TranscriptTargets" %>

<%-- Display if the TranscriptTargets object is not null and we have either a
dblink or some relatedMarkers to show --%>

<zfin2:subsection title="${fn:toUpperCase('Target Genes')}" test="${!empty transcriptTargets
              && (!empty transcriptTargets.predictedTarget
                  || !empty transcriptTargets.publishedTargets)}">

    <table class="summary solidblock transcripttargets">

        <c:if test="${!empty transcriptTargets.predictedTarget}">
            <tr>
                <td nowrap="nowrap" width="20%">Predicted Targets:</td>
                <td>
                    <zfin:link entity="${transcriptTargets.predictedTarget}"/>
                </td>
            </tr>
        </c:if>

        <c:if test="${!empty transcriptTargets.publishedTargets}">
            <tr>
                <td nowrap="nowrap" width="20%">Confirmed Targets:</td>
                <td>
                    <c:forEach var="relatedMarker" items="${transcriptTargets.publishedTargets}" varStatus="loop">
                        <zfin:link entity="${relatedMarker.marker}"/>
                        <zfin:attribution entity="${relatedMarker}"/><c:if test="${!loop.last}">,</c:if>
                    </c:forEach>
                </td>
            </tr>
        </c:if>
    </table>

</zfin2:subsection>
