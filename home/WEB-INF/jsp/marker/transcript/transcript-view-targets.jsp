<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>
<c:set var="transcriptTargets" value="${formBean.transcriptTargets}"/>

<%-- Display if the TranscriptTargets object is not null and we have either a
dblink or some relatedMarkers to show --%>

<z:attributeList>
    <c:if test="${!empty transcriptTargets.predictedTarget}">
        <z:attributeListItem label="Predicted Targets:">
            <zfin:link entity="${transcriptTargets.predictedTarget}"/>
        </z:attributeListItem>
    </c:if>

    <c:if test="${!empty transcriptTargets.publishedTargets}">
        <z:attributeListItem label="Confirmed Targets">
            <ul class="comma-separated">
                <c:forEach var="relatedMarker" items="${transcriptTargets.publishedTargets}">
                    <li><zfin:link entity="${relatedMarker.marker}"/> <zfin:attribution entity="${relatedMarker}"/></li>
                </c:forEach>
            </ul>
        </z:attributeListItem>
    </c:if>

</z:attributeList>
