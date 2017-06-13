<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="numPubs" type="java.lang.Integer" rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<div class="summary">
    <c:choose>
        <c:when test="${numPubs > 0}">
            <a href="/action/marker/citation-list/${marker.zdbID}">CITATIONS</a> (${numPubs})
        </c:when>
        <c:otherwise>
            <b>CITATIONS</b> (0)
        </c:otherwise>
    </c:choose>
</div>

