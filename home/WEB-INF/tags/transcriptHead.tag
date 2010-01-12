<%@ tag import="org.zfin.framework.presentation.EntityPresentation" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="transcript" type="org.zfin.marker.Transcript"
              rtexprvalue="true" required="true" %>

<div style="font-size: large; font-weight: bold; margin-top: 1em;">
Transcript Name: <zfin:name entity="${transcript}"/>
</div>
<div style="font-size: large; font-weight: bold;">
<a href="/action/marker/transcript-definitions#type">Transcript Type</a>:
<span title="${transcript.transcriptType.definition}">${transcript.transcriptType.display}</span>
</div>

<c:if test="${!empty transcript.status}">
    <div style="font-size: large; font-weight: bold;">
    <a href="/action/marker/transcript-definitions#status">Annotation Status</a>: ${transcript.status.display}
        <c:if test="${transcript.withdrawn}">
            <%=EntityPresentation.WITHDRAWN%>
        </c:if>
    </div>
</c:if>

<zfin2:previousNames entity="${transcript}"/>

<zfin2:notes hasNotes="${transcript}"/>
