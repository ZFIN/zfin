<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:choose>
    <c:when test="${!empty errors.allErrors}">
        Failed to delete <zfin:link entity="${formBean.markerToDelete}"/>
        <zfin2:handleErrors bindExeption="${errors}"/>
        <a href="/action/marker/marker-edit?zdbID=${formBean.zdbIDToDelete}">[Edit]</a>

    </c:when>
    <c:otherwise>
        <b>${formBean.markerToDeleteViewString}</b> has been deleted!
    </c:otherwise>
</c:choose>
