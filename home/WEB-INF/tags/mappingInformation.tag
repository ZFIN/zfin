<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="mappedMarker" type="org.zfin.mapping.presentation.MappedMarkerBean"
              rtexprvalue="true" required="true" %>

<hr width="80%"/>
<div class="summary">
<table class="summary solidblock mappinginformation">
    <caption>MAPPING INFORMATION:</caption>
    <tr>
        <c:choose>
            <c:when test="${empty mappedMarker.unMappedMarkers}">
                <td>None submitted</td>
            </c:when>
            <c:otherwise>
                <td>
                    LG:
                    <c:forEach var="lg" items="${mappedMarker.unMappedMarkers}" varStatus="index">
                        ${!index.first ? "," : "" }
                        ${lg}
                    </c:forEach>
                    <a href="<%=ZfinProperties.getWebDriver()%>?MIval=aa-mappingdetail.apg&OID=${mappedMarker.marker.zdbID}">Details</a>

                </td>

                <c:if test="${mappedMarker.hasMappedMarkers}">
                    <td align="right">
                        View Map:
                        <a href="/cgi-bin/view_zmapplet.cgi?&userid=GUEST&OID=${mappedMarker.marker.zdbID}&view_map=view_map&ZMAP=1&LN54=1&T51=1&MGH=1&HS=1&MOP=1&GAT=1">Merged</a>
                        &nbsp;
                        <a href="/cgi-bin/view_mapplet.cgi?&userid=GUEST&OID=${mappedMarker.marker.zdbID}&view_map=view_map&ZMAP=1&LN54=1&T51=1&MGH=1&HS=1&MOP=1&GAT=1">Individual Panels</a>
                    </td>
                </c:if>
            </c:otherwise>
        </c:choose>
    </tr>
</table>
</div>



