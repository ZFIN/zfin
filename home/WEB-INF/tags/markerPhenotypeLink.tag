<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="phenotypeOnMarkerBean" required="true" rtexprvalue="true"
              type="org.zfin.marker.presentation.PhenotypeOnMarkerBean" %>
<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>

<c:choose>
    <c:when test="${phenotypeOnMarkerBean.numFigures==1}">
        ${phenotypeOnMarkerBean.singleFigureLink.link}
    </c:when>
    <c:otherwise>
        <a href="/action/marker/${marker.zdbID}/phenotype-summary">
                ${phenotypeOnMarkerBean.numFigures} figures</a>
    </c:otherwise>
</c:choose>
from
<c:choose>
    <c:when test="${phenotypeOnMarkerBean.numPublications==1}">
        ${phenotypeOnMarkerBean.singlePublicationLink.link}
    </c:when>
    <c:otherwise>
        ${phenotypeOnMarkerBean.numPublications} publications
    </c:otherwise>
</c:choose>