<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="mappedMarker" rtexprvalue="true" required="false" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker" required="false" %>

<a href="/action/mapping/detail/${marker.zdbID}">(Mapping Details/Browser)</a>
