<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="mappedMarker" type="org.zfin.mapping.presentation.MappedMarkerBean"
              rtexprvalue="true" required="true" %>

<div class="summary">
    <table class="summary solidblock mappinginformation">
        <caption>MAPPING INFORMATION
            <c:if test="${empty mappedMarker.unMappedMarkers}">
                <span class="no-data-tag">Unknown</span>
            </c:if>

        </caption>
        <tr>
            <c:if test="${!empty mappedMarker.unMappedMarkers}">
                <td width="25%">
                    LG:
                    <c:choose>
                        <c:when test="${fn:length(mappedMarker.unMappedMarkers)==1 and mappedMarker.unMappedMarkers[0] eq '0'}">
                            Unknown
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="lg" items="${mappedMarker.unMappedMarkers}" varStatus="index">
                                <c:if test="${lg ne '0'}">
                                    ${lg}${!index.last? ", " : "" }
                                </c:if>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-mappingdetail.apg&OID=${mappedMarker.marker.zdbID}">Details</a>

                </td>

                <c:if test="${mappedMarker.hasMappedMarkers}">
                    <td align="right">
                        View Map:
                        <a href="/cgi-bin/view_zmapplet.cgi?&userid=GUEST&OID=${mappedMarker.marker.zdbID}&view_map=view_map&ZMAP=1&LN54=1&T51=1&MGH=1&HS=1&MOP=1&GAT=1">Merged</a>
                        &nbsp;
                        <a href="/cgi-bin/view_mapplet.cgi?&userid=GUEST&OID=${mappedMarker.marker.zdbID}&view_map=view_map&ZMAP=1&LN54=1&T51=1&MGH=1&HS=1&MOP=1&GAT=1">Individual Panels</a>
                    </td>
                </c:if>
            </c:if>
        </tr>
    </table>
</div>



