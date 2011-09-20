<%@ tag import="org.zfin.framework.presentation.EntityPresentation" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="transcript" type="org.zfin.marker.Transcript"
              rtexprvalue="true" required="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="relatedGenes" type="java.util.Set" rtexprvalue="true" required="true" %>
<%@ attribute name="strain" type="org.zfin.mutant.Genotype" rtexprvalue="true" required="true" %>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Transcript&nbsp;Name:</span></th>
        <td><span class="name-value"><zfin:name entity="${transcript}"/></span></td>
    </tr>
    <c:if test="${!empty previousNames}">
        <zfin2:previousNamesFast label="Previous Names:" previousNames="${previousNames}"/>
    </c:if>
    <tr>
        <th>Transcript&nbsp;Type:<a class="popup-link info-popup-link" href="/action/marker/transcript-types"></a></th>
        <td><span title="${transcript.transcriptType.definition}">${transcript.transcriptType.display}</span></td>
    </tr>
    <c:if test="${!empty transcript.status}">
        <tr>
            <th>Annotation&nbsp;Status:<a class="popup-link info-popup-link" href="/action/marker/transcript-statuses"></a></th>
            <td>
                    ${transcript.status.display}
                <c:if test="${transcript.withdrawn}">
                    <%=EntityPresentation.WITHDRAWN%>
                </c:if>
            </td>
        </tr>
    </c:if>
    <c:if test="${!empty strain}">
        <tr>
            <th>Strain:</th>
            <td>
                    <zfin:link entity="${strain}"/>
            </td>
        </tr>
    </c:if>
    <tr><th>Associated with Genes:</th>
        <td>
        <zfin2:toggledHyperlinkList collection="${relatedGenes}"
                                    id="relatedGenes"
                                    maxNumber="6"
                                    showAttributionLinks="true"/>
        </td></tr>

    <zfin2:notesInDiv hasNotes="${transcript}"/>
</table>




