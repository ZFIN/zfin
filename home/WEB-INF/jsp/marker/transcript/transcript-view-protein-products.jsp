<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<z:dataList hasData="${!empty formBean.proteinProductDBLinkDisplay}">
    <c:forEach var="entry" items="${formBean.proteinProductDBLinkDisplay}">
        <%-- entry.value is the MarkerDBLink --%>
        <c:forEach var="dblink" items="${entry.value}">
            <li>
                <zfin:link entity="${dblink}"/> <zfin:attribution entity="${dblink}"/>
            </li>
        </c:forEach>
    </c:forEach>
</z:dataList>