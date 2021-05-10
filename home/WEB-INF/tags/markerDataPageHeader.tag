<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" required="true" type="org.zfin.marker.Marker" %>

<div class="small text-uppercase text-muted">${marker.markerType.displayName}</div>
<h1><zfin:abbrev entity="${marker}"/></h1>
