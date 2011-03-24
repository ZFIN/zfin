<%@ tag import="org.zfin.framework.presentation.EntityPresentation" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="transcript" type="org.zfin.marker.Transcript"
              rtexprvalue="true" required="true" %>

<table class="primary-entity-attributes">
  <tr>
      <th><span class="name-label">Transcript&nbsp;Name:</span></th>
      <td><span class="name-value"><zfin:name entity="${transcript}"/></span></td>
  </tr>
  <tr>
      <th><a href="/action/marker/transcript-definitions#type">Transcript&nbsp;Type</a>:</th>
      <td><span title="${transcript.transcriptType.definition}">${transcript.transcriptType.display}</span></td>
  </tr>
  <c:if test="${!empty transcript.status}">
  <tr>
      <th><a href="/action/marker/transcript-definitions#status">Annotation&nbsp;Status</a>:</th>
      <td>
          ${transcript.status.display}
          <c:if test="${transcript.withdrawn}">
              <%=EntityPresentation.WITHDRAWN%>
          </c:if>
      </td>
  </tr>
  </c:if>
  <zfin2:previousNames entity="${transcript}"/>
</table>




<zfin2:notes hasNotes="${transcript}"/>
