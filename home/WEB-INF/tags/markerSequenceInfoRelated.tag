<%@ attribute name="title" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="dbLinks" type="java.util.List" rtexprvalue="true" required="true" %>





<%--<span class="summaryTitle"><zfin:link entity="${marker}"/> Does something:</span>--%>
<c:set var="title"><zfin:link entity="${marker}"/> ${title}</c:set>

<c:if test="${!empty dbLinks}">
    <zfin2:subsection title="${title}">

        <table class="summary rowstripes">
            <tr>
                <th width=20%>Marker</th>
                <th width=10%>Type</th>
                <th width=40%>Accession #</th>
                <th class="length">Length (bp/aa)</th>
                <th class="analysis">Analysis <a class="popup-link info-popup-link" href="/ZFIN/help_files/sequence_tools_help.html"></a></th>
            </tr>

            <c:set var="markerAbbrev"/>
            <c:set var="cssClass"/>
            <c:set var="newAbbrev"/>
            <c:set var="row" value="odd"/>
            <c:set var="group" value="oddgroup"/>



            <c:forEach var="dblink" items="${dbLinks}" varStatus="loop">

                <c:set var="newAbbrev" value="${empty markerAbbrev or (markerAbbrev ne dblink.marker.abbreviation)}"/>
                <c:set var="row" value="${loop.index%2==0 ? ' odd ' : ' even '}"/>
                <c:if test="${newAbbrev}">
                    <c:set var="group" value="${ group eq 'oddgroup'  ? ' evengroup ' : ' oddgroup '}"/>
                </c:if>
                <c:set var="cssClass" value="${row} ${newAbbrev ? 'newgroup' : ''} ${group}"/>

                <tr class="${cssClass}">

                    <td>
                        <c:choose>
                            <c:when test="${newAbbrev}">
                                [${dblink.marker.type}]
                                <zfin:link entity="${dblink.marker}"/>
                                <zfin:attribution entity="${dblink.marker}"/>
                            </c:when>
                            <c:otherwise>
                                &nbsp;
                            </c:otherwise>
                        </c:choose>

                        <c:set var="markerAbbrev" value="${dblink.marker.abbreviation}"/>
                    </td>
                    <td>
                            ${dblink.referenceDatabase.foreignDBDataType.dataType}
                    </td>
                    <td>
                        <zfin:link entity="${dblink}"/>
                    </td>
                    <td>
                            ${dblink.length}
                    </td>
                    <td  style="text-align: center">
                        <zfin2:externalAccessionBlastDropDown dbLink="${dblink}"/>
                    </td>
                </tr>
            </c:forEach>
        </table>

    </zfin2:subsection>
</c:if>


