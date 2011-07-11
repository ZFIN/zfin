<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="relationships" required="true"
              rtexprvalue="true" type="org.zfin.marker.presentation.RelatedMarkerDisplay" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>


<%@ attribute name="title" required="false"%>



<c:if test="${empty title}">
    <c:set var="title" value="MARKER RELATIONSHIPS"/>
</c:if>

<zfin2:subsection title="${title}"
                  test="${!empty relationships}">

    <table class="summary horizontal-solidblock">
        <c:forEach var="entry" items="${relationships}">
            <tr>
                <td class="data-label">${entry.key}:<%-- relationship label --%></td>
                <td>
                    <c:forEach var="typeMap" items="${entry.value}" >
                        <div>
                            <small>[${typeMap.key.displayName}]</small> <%-- marker type --%>
                            <%-- the nasty long line is to remove spaces before commas --%>
                            <c:forEach var="relatedMarker" items="${typeMap.value}" varStatus="insideLoop">
                                <zfin:link entity="${relatedMarker.marker}"/><zfin:attribution entity="${relatedMarker}"/><c:forEach var="supplier" items="${relatedMarker.marker.suppliers}"><small> (<a href="${supplier.orderURL}${supplier.accNum}">${supplier.organization.organizationOrderURL.hyperlinkName}</a>)</small></c:forEach><c:if test="${!insideLoop.last}">,</c:if>
                            </c:forEach>
                        </div>
                    </c:forEach>

                </td>
            </tr>
        </c:forEach>
    </table>

</zfin2:subsection>
