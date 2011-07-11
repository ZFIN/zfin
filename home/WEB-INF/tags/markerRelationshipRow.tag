<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="marker" required="true" rtexprvalue="true"
              type="org.zfin.marker.Marker" %>
<%@ attribute name="relationship" required="true" rtexprvalue="true"
              type="org.zfin.marker.presentation.MarkerRelationshipPresentation" %>
<%@ attribute name="newRelationshipType" required="true" type="java.lang.Boolean" %>
<%@ attribute name="newMarkerType" required="true" type="java.lang.Boolean" %>

<c:if test="${newRelationshipType}">
    <tr>
    <td>${marker.abbreviation} ${relationship.relationshipType}</td>
    <td>
</c:if>
[${relationship.markerType}] <a href="">${relationship.abbreviation}</a> (<a href="">1</a>)
${(newMarkerType ? "<br>" : "")}
<c:if test="${newRelationshipType}">
    </td>
    </tr>
</c:if>

<%--<tr>--%>
<%--<th>${fn:replace(entry.key," ","&nbsp;")} &lt;%&ndash; relationship label &ndash;%&gt;</th>--%>
<%--<td>--%>
<%--<c:forEach var="typeMap" items="${entry.value}" >--%>
<%--<div>--%>
<%--<small>[${typeMap.key.displayName}]</small> &lt;%&ndash; marker type &ndash;%&gt;--%>
<%--&lt;%&ndash; the nasty long line is to remove spaces before commas &ndash;%&gt;--%>
<%--<c:forEach var="relatedMarker" items="${typeMap.value}" varStatus="insideLoop">--%>
<%--<zfin:link entity="${relatedMarker.marker}"/><zfin:attribution entity="${relatedMarker}"/><c:forEach var="supplier" items="${relatedMarker.marker.suppliers}"><small> (<a href="${supplier.orderURL}${supplier.accNum}">${supplier.organization.organizationOrderURL.hyperlinkName}</a>)</small></c:forEach><c:if test="${!insideLoop.last}">,</c:if>--%>
<%--</c:forEach>--%>
<%--</div>--%>
<%--</c:forEach>--%>

<%--</td>--%>
<%--</tr>--%>

