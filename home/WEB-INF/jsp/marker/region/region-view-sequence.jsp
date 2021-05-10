<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="marker" value="${sformBean.marker}"/>
<c:set var="sequenceInfo" value="${formBean.sequenceInfo}"/>

<z:dataTable collapse="true"
             hasData="${!empty sequenceInfo && !empty sequenceInfo.dbLinks}">
    <tr>
        <th width="25%">Type</th>
        <th width="25%"> Accession #</th>
        <th width="15%" style="text-align: right"> Length (nt/aa)</th>
        <th width="35%" style="text-align: center">
            Analysis <a class="popup-link info-popup-link" href="/ZFIN/help_files/sequence_tools_help.html"></a>
        </th>
    </tr>

    <c:set var="lastType" value=""/>
    <c:set var="groupIndex" value="0"/>
    <c:forEach var="dblink" items="${sequenceInfo.dbLinks}" varStatus="loop">
        <tr class=${loop.index%2==0 ? "even" : "odd"}>
            <td>
                    ${dblink.referenceDatabase.foreignDBDataType.dataType.toString()}
            </td>
            <td>
                <zfin:link entity="${dblink}"/>
                <zfin:attribution entity="${dblink}"/>
            </td>
            <td style="text-align: right">
                <c:if test="${!empty dblink.length}"> ${dblink.length} ${dblink.units} </c:if>
            </td>
            <td style="text-align: center; margin-left: 100px;">
                <zfin2:externalAccessionBlastDropDown dbLink="${dblink}"/>
            </td>
                <%--</zfin:alternating-tr>--%>
        </tr>
        <%--</c:if>--%>
        <%--<c:set var="lastType" value="${dblink.referenceDatabase.foreignDBDataType.dataType}"/>--%>
    </c:forEach>
    <c:if test="${sequenceInfo.hasMoreLinks}">
        <a href="/action/marker/sequence/view/${marker.zdbID}">Sequence Information
            (all ${sequenceInfo.numberDBLinks})</a>
    </c:if>
</z:dataTable>