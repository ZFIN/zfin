<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="numPubs" type="java.lang.Integer" rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<div class="summary">
    <c:choose>
        <c:when test="${numPubs > 0}">
            <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${marker.zdbID}&rtype=marker&title=${marker.markerType.displayName}&name=${marker.name}&abbrev=${marker.abbreviation}&total_count=${numPubs}">CITATIONS</a> (${numPubs})
        </c:when>
        <c:otherwise>
            <b>CITATIONS</b> (0)
        </c:otherwise>
    </c:choose>
</div>
