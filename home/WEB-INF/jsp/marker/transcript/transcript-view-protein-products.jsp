<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<z:dataTable collapse="true" hasData="${!empty formBean.relatedTranscriptDisplay.nonWithdrawnTranscripts}">

    <thead>
<tr>
    <th>Protein</th>
</tr>
</thead>
    <tbody>
    <c:forEach var="entry" items="${formBean.summaryDBLinkDisplay}">
        <td>
                <%-- entry.value is the MarkerDBLink --%>
            <c:forEach var="dblink" items="${entry.value}">
                <li style="list-style-type:none;">
                    <zfin:link entity="${dblink}"/>
                    <zfin:attribution entity="${dblink}"/>
                </li>
            </c:forEach>
        </td>
    </c:forEach>

    </tbody>

</z:dataTable>