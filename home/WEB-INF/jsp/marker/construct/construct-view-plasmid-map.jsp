<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<c:choose>
    <c:when test="${!empty formBean.marker.figures}">
        <c:forEach var="fig" items="${formBean.marker.figures}">

            <c:forEach var="img" items="${fig.images}">
                <a href="/${img.zdbID}"><img src="/imageLoadUp/${img.imageFilename}" width="300"
                                             height="200"></a>
            </c:forEach>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <span class="no-data-tag"><i>No data available</i></span>
    </c:otherwise>
</c:choose>

