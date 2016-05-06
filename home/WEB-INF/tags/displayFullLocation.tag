<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="location" required="true" type="org.zfin.mapping.FeatureGenomeLocation" %>
<%@ attribute name="hideLink" required="false" type="java.lang.Boolean" %>

Chr ${location.chromosome}: ${location.start}
<c:if test="${!empty location.end && location.end != location.end}">
    - ${location.end}
</c:if>
<c:if test="${!empty location.assembly}">
    (${location.assembly})
</c:if>
<c:if test="${!empty location.attribution}">
    (<a href="/${location.attribution.zdbID}">1</a>)
</c:if>
<c:if test="${!hideLink}">
    <a href="/action/mapping/detail/${location.feature.zdbID}">Details</a>
</c:if>