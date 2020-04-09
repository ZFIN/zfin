<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<z:dataTable collapse="true"
             hasData="${!empty formBean.sequenceInfo && !empty formBean.sequenceInfo.dbLinks}">

    <thead>
    <tr>
        <th>Type</th>
        <th> Accession # </th>
        <th> Length (nt/aa) </th>
        <th>
            Analysis <a class="popup-link info-popup-link" href="/ZFIN/help_files/sequence_tools_help.html"></a>
        </th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="dblink" items="${sequenceInfo.dbLinks}" varStatus="loop">
        <tr>
            <td>
                    ${dblink.referenceDatabase.foreignDBDataType.dataType.toString()}
            </td>
            <td>
                <zfin:link entity="${dblink}"/>
                <zfin:attribution entity="${dblink}"/>
            </td>
            <td>
                <c:if test="${!empty dblink.length}"> ${dblink.length} ${dblink.units} </c:if>
            </td>
            <td>
                <zfin2:blastDropDown dbLink="${dblink}"/>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</z:dataTable>