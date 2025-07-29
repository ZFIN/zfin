<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="annotationStatus" %>

<z:attributeListItem>
    <jsp:attribute name="label">
        Annotation Status <a class='popup-link info-popup-link' href='/action/marker/note/annotation-status-desc'></a>
    </jsp:attribute>
    <jsp:body>
        ${annotationStatus}
    </jsp:body>
</z:attributeListItem>
